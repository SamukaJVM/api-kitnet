package com.kitnet.domain.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kitnet.domain.exception.ApartamentoNaoEncontradoException;
import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.model.Predio;
import com.kitnet.domain.repository.ApartamentoRepository;
import com.kitnet.domain.repository.PredioRepository;

@Service
public class CadastroApartamentoService {

	private static final String MSG_NUMERO_DUPLICADO = "Já existe um apartamento com esse número neste prédio";
	private static final String MSG_MEDIDOR_DUPLICADO = "Já existe um apartamento cadastrado com esse medidor";

	private final ApartamentoRepository apartamentoRepository;
	private final PredioRepository predioRepository;

	public CadastroApartamentoService(ApartamentoRepository apartamentoRepository, PredioRepository predioRepository) {
		this.apartamentoRepository = apartamentoRepository;
		this.predioRepository = predioRepository;
	}

	@Transactional
	public Apartamento criar(Apartamento apartamento, Long predioId) {
		Predio predio = buscarPredioOuFalhar(predioId);
		apartamento.setPredio(predio);
		normalizarMedidor(apartamento);
		validarNumeroDuplicado(apartamento, null);
		validarMedidorDuplicado(apartamento, null);
		return persistir(apartamento);
	}

	@Transactional
	public Apartamento atualizar(Long apartamentoId, Apartamento dados, Long predioId) {
		Predio predio = buscarPredioOuFalhar(predioId);
		dados.setPredio(predio);
		normalizarMedidor(dados);
		validarNumeroDuplicado(dados, apartamentoId);
		validarMedidorDuplicado(dados, apartamentoId);

		Apartamento apartamento = buscarOuFalhar(apartamentoId);
		apartamento.atualizarDados(dados);
		return persistir(apartamento);
	}

	@Transactional
	public void excluir(Long apartamentoId) {
		Apartamento apartamento = buscarOuFalhar(apartamentoId);
		try {
			apartamentoRepository.delete(apartamento);
			apartamentoRepository.flush();
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException("Apartamento não pode ser excluído pois possui lançamentos associados");
		}
	}

	@Transactional(readOnly = true)
	public Apartamento buscarOuFalhar(Long apartamentoId) {
		return apartamentoRepository.findById(apartamentoId)
			.orElseThrow(() -> new ApartamentoNaoEncontradoException("Apartamento não encontrado com id: " + apartamentoId));
	}

	@Transactional(readOnly = true)
	public Page<Apartamento> listar(Long predioId, Pageable pageable) {
		Specification<Apartamento> spec = (root, query, cb) -> predioId == null
			? cb.conjunction()
			: cb.equal(root.get("predio").get("id"), predioId);
		return apartamentoRepository.findAll(spec, pageable);
	}

	private Predio buscarPredioOuFalhar(Long predioId) {
		return predioRepository.findById(predioId)
			.orElseThrow(() -> new NegocioException("Prédio não encontrado com id: " + predioId));
	}

	private void normalizarMedidor(Apartamento apartamento) {
		if (apartamento.getMedidor() != null && apartamento.getMedidor().isBlank()) {
			apartamento.setMedidor(null);
		}
	}

	private void validarNumeroDuplicado(Apartamento apartamento, Long idAtual) {
		Long predioId = apartamento.getPredio().getId();
		String numero = apartamento.getNumero();
		boolean duplicado = idAtual == null
			? apartamentoRepository.existsByPredioIdAndNumero(predioId, numero)
			: apartamentoRepository.existsByPredioIdAndNumeroAndIdNot(predioId, numero, idAtual);
		if (duplicado) {
			throw new NegocioException(MSG_NUMERO_DUPLICADO);
		}
	}

	private void validarMedidorDuplicado(Apartamento apartamento, Long idAtual) {
		String medidor = apartamento.getMedidor();
		if (medidor == null) {
			return;
		}
		boolean duplicado = idAtual == null
			? apartamentoRepository.existsByMedidor(medidor)
			: apartamentoRepository.existsByMedidorAndIdNot(medidor, idAtual);
		if (duplicado) {
			throw new NegocioException(MSG_MEDIDOR_DUPLICADO);
		}
	}

	private Apartamento persistir(Apartamento apartamento) {
		try {
			Apartamento saved = apartamentoRepository.save(apartamento);
			apartamentoRepository.flush();
			return saved;
		} catch (DataIntegrityViolationException e) {
			throw new NegocioException("Não foi possível salvar o apartamento: conflito com dados já cadastrados");
		}
	}

}
