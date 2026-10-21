package com.kitnet.domain.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.InquilinoNaoEncontradoException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.model.Inquilino;
import com.kitnet.domain.model.StatusInquilino;
import com.kitnet.domain.repository.InquilinoRepository;

@Service
public class CadastroInquilinoService {

	private static final String MSG_CPF_DUPLICADO = "Já existe um inquilino cadastrado com esse CPF";

	private final InquilinoRepository inquilinoRepository;

	public CadastroInquilinoService(InquilinoRepository inquilinoRepository) {
		this.inquilinoRepository = inquilinoRepository;
	}

	@Transactional
	public Inquilino criar(Inquilino inquilino) {
		validarCpfDuplicado(inquilino, null);
		return persistir(inquilino);
	}

	@Transactional
	public Inquilino atualizar(Long inquilinoId, Inquilino dados) {
		Inquilino inquilino = buscarOuFalhar(inquilinoId);
		validarCpfDuplicado(dados, inquilinoId);

		inquilino.atualizarDados(dados);
		return persistir(inquilino);
	}

	@Transactional
	public Inquilino inativar(Long inquilinoId) {
		Inquilino inquilino = buscarOuFalhar(inquilinoId);
		inquilino.setStatus(StatusInquilino.INATIVO);
		return persistir(inquilino);
	}

	@Transactional
	public Inquilino reativar(Long inquilinoId) {
		Inquilino inquilino = buscarOuFalhar(inquilinoId);
		inquilino.setStatus(StatusInquilino.ATIVO);
		return persistir(inquilino);
	}

	@Transactional
	public void excluir(Long inquilinoId) {
		Inquilino inquilino = buscarOuFalhar(inquilinoId);
		try {
			inquilinoRepository.delete(inquilino);
			inquilinoRepository.flush();
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException("Inquilino não pode ser excluído pois possui lançamentos associados");
		}
	}

	@Transactional(readOnly = true)
	public Inquilino buscarOuFalhar(Long inquilinoId) {
		return inquilinoRepository.findById(inquilinoId)
			.orElseThrow(() -> new InquilinoNaoEncontradoException("Inquilino não encontrado com id: " + inquilinoId));
	}

	@Transactional(readOnly = true)
	public Page<Inquilino> listar(StatusInquilino status, Pageable pageable) {
		Specification<Inquilino> spec = (root, query, cb) -> status == null
			? cb.conjunction()
			: cb.equal(root.get("status"), status);
		return inquilinoRepository.findAll(spec, pageable);
	}

	private void validarCpfDuplicado(Inquilino inquilino, Long idAtual) {
		String cpf = inquilino.getCpf();
		if (cpf == null) {
			return;
		}
		boolean duplicado = idAtual == null
			? inquilinoRepository.existsByCpf(cpf)
			: inquilinoRepository.existsByCpfAndIdNot(cpf, idAtual);
		if (duplicado) {
			throw new NegocioException(MSG_CPF_DUPLICADO);
		}
	}

	private Inquilino persistir(Inquilino inquilino) {
		try {
			Inquilino saved = inquilinoRepository.save(inquilino);
			inquilinoRepository.flush();
			return saved;
		} catch (DataIntegrityViolationException e) {
			throw new NegocioException("Não foi possível salvar o inquilino: conflito com dados já cadastrados");
		}
	}

}
