package com.kitnet.domain.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.exception.PredioNaoEncontradoException;
import com.kitnet.domain.model.Predio;
import com.kitnet.domain.repository.PredioRepository;

@Service
public class CadastroPredioService {

	private static final String MSG_DESCRICAO_DUPLICADA = "Já existe um prédio cadastrado com essa descrição";

	private final PredioRepository predioRepository;

	public CadastroPredioService(PredioRepository predioRepository) {
		this.predioRepository = predioRepository;
	}

	@Transactional
	public Predio criar(Predio predio) {
		if (predioRepository.existsByDescricao(predio.getDescricao())) {
			throw new NegocioException(MSG_DESCRICAO_DUPLICADA);
		}
		return persistir(predio);
	}

	@Transactional
	public void excluir(Long predioId) {
		Predio predio = buscarOuFalhar(predioId);
		try {
			predioRepository.delete(predio);
			predioRepository.flush();
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException("Prédio não pode ser excluído pois possui apartamentos associados");
		}
	}

	@Transactional(readOnly = true)
	public Predio buscarOuFalhar(Long predioId) {
		return predioRepository.findById(predioId)
			.orElseThrow(() -> new PredioNaoEncontradoException("Prédio não encontrado com id: " + predioId));
	}

	@Transactional(readOnly = true)
	public Page<Predio> listar(Pageable pageable) {
		return predioRepository.findAll(pageable);
	}

	@Transactional
	public Predio atualizar(Long predioId, Predio predioAtualizado) {
		if (predioRepository.existsByDescricaoAndIdNot(predioAtualizado.getDescricao(), predioId)) {
			throw new NegocioException(MSG_DESCRICAO_DUPLICADA);
		}
		Predio predio = buscarOuFalhar(predioId);
		predio.atualizarDados(predioAtualizado);
		return persistir(predio);
	}

	private Predio persistir(Predio predio) {
		try {
			Predio saved = predioRepository.save(predio);
			predioRepository.flush();
			return saved;
		} catch (DataIntegrityViolationException e) {
			throw new NegocioException("Não foi possível salvar o prédio: conflito com dados já cadastrados");
		}
	}

}
