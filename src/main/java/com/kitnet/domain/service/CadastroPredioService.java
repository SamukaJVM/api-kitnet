package com.kitnet.domain.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.exception.PredioNaoEncontradoException;
import com.kitnet.domain.model.Predio;
import com.kitnet.domain.repository.PredioRepository;

@Service
public class CadastroPredioService {

	private final PredioRepository predioRepository;

	public CadastroPredioService(PredioRepository predioRepository) {
		this.predioRepository = predioRepository;
	}

	@Transactional
	public Predio salvar(Predio predio) {
		if (predio.getId() == null) {
			if (predioRepository.existsByDescricao(predio.getDescricao())) {
				throw new NegocioException("Já existe um prédio cadastrado com essa descrição");
			}
		} else {
			if (predioRepository.existsByDescricaoAndIdNot(predio.getDescricao(), predio.getId())) {
				throw new NegocioException("Já existe um prédio cadastrado com essa descrição");
			}
		}
		try {
			Predio saved = predioRepository.save(predio);
			predioRepository.flush();
			return saved;
		} catch (DataIntegrityViolationException e) {
			throw new NegocioException("Já existe um prédio cadastrado com essa descrição");
		}
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

}
