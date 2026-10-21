package com.kitnet.api.v1.controller;

import java.net.URI;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.mapper.InquilinoMapper;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.api.v1.openapi.InquilinoControllerOpenApi;
import com.kitnet.domain.model.Inquilino;
import com.kitnet.domain.model.StatusInquilino;
import com.kitnet.domain.service.CadastroInquilinoService;

@RestController
@RequestMapping("/v1/inquilinos")
public class InquilinoController implements InquilinoControllerOpenApi {

	private final CadastroInquilinoService cadastroInquilinoService;
	private final InquilinoMapper inquilinoMapper;

	public InquilinoController(
			CadastroInquilinoService cadastroInquilinoService,
			InquilinoMapper inquilinoMapper) {
		this.cadastroInquilinoService = cadastroInquilinoService;
		this.inquilinoMapper = inquilinoMapper;
	}

	@GetMapping
	@Override
	public Page<InquilinoModel> listar(
			@RequestParam(required = false) @Nullable StatusInquilino status,
			Pageable pageable) {
		return cadastroInquilinoService.listar(status, pageable)
				.map(inquilinoMapper::toModel);
	}

	@GetMapping("/{id}")
	@Override
	public ResponseEntity<InquilinoModel> buscar(@PathVariable Long id) {
		Inquilino inquilino = cadastroInquilinoService.buscarOuFalhar(id);
		return ResponseEntity.ok(inquilinoMapper.toModel(inquilino));
	}

	@PostMapping
	@Override
	public ResponseEntity<InquilinoModel> criar(@RequestBody @Valid InquilinoInput inquilinoInput) {
		Inquilino inquilino = inquilinoMapper.toDomain(inquilinoInput);
		inquilino = cadastroInquilinoService.criar(inquilino);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(inquilino.getId())
				.toUri();

		return ResponseEntity.created(location).body(inquilinoMapper.toModel(inquilino));
	}

	@PutMapping("/{id}")
	@Override
	public ResponseEntity<InquilinoModel> atualizar(
			@PathVariable Long id,
			@RequestBody @Valid InquilinoInput inquilinoInput) {
		Inquilino dados = inquilinoMapper.toDomain(inquilinoInput);
		Inquilino inquilino = cadastroInquilinoService.atualizar(id, dados);
		return ResponseEntity.ok(inquilinoMapper.toModel(inquilino));
	}

	@DeleteMapping("/{id}")
	@Override
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		cadastroInquilinoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/inativacao")
	@Override
	public ResponseEntity<Void> inativar(@PathVariable Long id) {
		cadastroInquilinoService.inativar(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/inativacao")
	@Override
	public ResponseEntity<Void> reativar(@PathVariable Long id) {
		cadastroInquilinoService.reativar(id);
		return ResponseEntity.noContent().build();
	}

}
