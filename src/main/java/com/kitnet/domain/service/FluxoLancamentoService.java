package com.kitnet.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kitnet.domain.exception.LancamentoNaoEncontradoException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.model.Inquilino;
import com.kitnet.domain.model.Lancamento;
import com.kitnet.domain.model.StatusApartamento;
import com.kitnet.domain.model.StatusInquilino;
import com.kitnet.domain.model.StatusLancamento;
import com.kitnet.domain.repository.ApartamentoRepository;
import com.kitnet.domain.repository.InquilinoRepository;
import com.kitnet.domain.repository.LancamentoRepository;

@Service
public class FluxoLancamentoService {

	private final LancamentoRepository lancamentoRepository;
	private final ApartamentoRepository apartamentoRepository;
	private final InquilinoRepository inquilinoRepository;

	public FluxoLancamentoService(
			LancamentoRepository lancamentoRepository,
			ApartamentoRepository apartamentoRepository,
			InquilinoRepository inquilinoRepository) {
		this.lancamentoRepository = lancamentoRepository;
		this.apartamentoRepository = apartamentoRepository;
		this.inquilinoRepository = inquilinoRepository;
	}

	@Transactional
	public Lancamento criar(Lancamento lancamento, Long apartamentoId, Long inquilinoId) {
		Apartamento apartamento = buscarApartamentoOuFalhar(apartamentoId);
		Inquilino inquilino = buscarInquilinoOuFalhar(inquilinoId);
		validarPrecondicoes(apartamento, inquilino);

		lancamento.setApartamento(apartamento);
		lancamento.setInquilino(inquilino);
		lancamento.setValorMensal(apartamento.getValorAluguel());
		lancamento.calcularValores();

		apartamento.ocupar();
		apartamentoRepository.save(apartamento);

		try {
			return lancamentoRepository.saveAndFlush(lancamento);
		} catch (DataIntegrityViolationException e) {
			throw mapearViolacaoDeConcorrencia(e);
		}
	}

	@Transactional
	public Lancamento registrarPagamento(Long lancamentoId, BigDecimal valorPago, @Nullable String observacao) {
		Lancamento lancamento = buscarOuFalhar(lancamentoId);
		if (lancamento.getStatus() == StatusLancamento.FECHADO) {
			throw new NegocioException("Lançamento fechado não pode ter o pagamento alterado");
		}
		lancamento.setValorPago(valorPago);
		lancamento.setObservacao(observacao);
		lancamento.calcularValores();
		return lancamentoRepository.save(lancamento);
	}

	@Transactional
	public void excluir(Long lancamentoId) {
		Lancamento lancamento = buscarOuFalhar(lancamentoId);
		if (lancamento.getStatus() == StatusLancamento.FECHADO) {
			throw new NegocioException("Lançamento fechado não pode ser excluído");
		}
		Apartamento apartamento = lancamento.getApartamento();
		apartamento.liberar();
		apartamentoRepository.save(apartamento);
		lancamentoRepository.delete(lancamento);
	}

	@Transactional(readOnly = true)
	public Lancamento buscarOuFalhar(Long lancamentoId) {
		return lancamentoRepository.findById(lancamentoId)
			.orElseThrow(() -> new LancamentoNaoEncontradoException("Lançamento não encontrado com id: " + lancamentoId));
	}

	@Transactional(readOnly = true)
	public Page<Lancamento> listar(@Nullable Long apartamentoId, @Nullable Long inquilinoId, @Nullable StatusLancamento status, Pageable pageable) {
		Specification<Lancamento> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (apartamentoId != null) {
				predicates.add(cb.equal(root.get("apartamento").get("id"), apartamentoId));
			}
			if (inquilinoId != null) {
				predicates.add(cb.equal(root.get("inquilino").get("id"), inquilinoId));
			}
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		return lancamentoRepository.findAll(spec, pageable);
	}

	private Apartamento buscarApartamentoOuFalhar(Long apartamentoId) {
		return apartamentoRepository.findById(apartamentoId)
			.orElseThrow(() -> new NegocioException("Apartamento não encontrado com id: " + apartamentoId));
	}

	private Inquilino buscarInquilinoOuFalhar(Long inquilinoId) {
		return inquilinoRepository.findById(inquilinoId)
			.orElseThrow(() -> new NegocioException("Inquilino não encontrado com id: " + inquilinoId));
	}

	private void validarPrecondicoes(Apartamento apartamento, Inquilino inquilino) {
		if (apartamento.getStatus() != StatusApartamento.DISPONIVEL) {
			throw new NegocioException("Apartamento não está disponível para locação");
		}
		if (inquilino.getStatus() != StatusInquilino.ATIVO) {
			throw new NegocioException("Inquilino inativo não pode receber novo lançamento");
		}
	}

	private RuntimeException mapearViolacaoDeConcorrencia(DataIntegrityViolationException e) {
		String constraint = extrairNomeConstraint(e);
		if ("ux_lancamento_apartamento_aberto".equals(constraint)) {
			return new NegocioException("Apartamento já possui um lançamento aberto", e);
		}
		if ("ux_lancamento_inquilino_aberto".equals(constraint)) {
			return new NegocioException("Inquilino já possui um lançamento aberto", e);
		}
		return e;
	}

	private @Nullable String extrairNomeConstraint(DataIntegrityViolationException e) {
		Throwable causa = e.getCause();
		if (causa instanceof ConstraintViolationException constraintViolationException) {
			return constraintViolationException.getConstraintName();
		}
		return null;
	}

}
