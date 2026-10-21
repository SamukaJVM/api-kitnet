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

import com.kitnet.api.v1.input.ApartamentoInput;
import com.kitnet.api.v1.mapper.ApartamentoMapper;
import com.kitnet.api.v1.model.ApartamentoModel;
import com.kitnet.api.v1.openapi.ApartamentoControllerOpenApi;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.service.CadastroApartamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/apartamentos")
public class ApartamentoController implements ApartamentoControllerOpenApi {

	private final CadastroApartamentoService cadastroApartamentoService;
	private final ApartamentoMapper apartamentoMapper;

	public ApartamentoController(
			CadastroApartamentoService cadastroApartamentoService,
			ApartamentoMapper apartamentoMapper) {
		this.cadastroApartamentoService = cadastroApartamentoService;
		this.apartamentoMapper = apartamentoMapper;
	}

	@GetMapping
	@Override
	public Page<ApartamentoModel> listar(
			@RequestParam(required = false) @Nullable Long predioId,
			Pageable pageable) {
		return cadastroApartamentoService.listar(predioId, pageable)
				.map(apartamentoMapper::toModel);
	}

	@GetMapping("/{id}")
	@Override
	public ResponseEntity<ApartamentoModel> buscar(@PathVariable Long id) {
		Apartamento apartamento = cadastroApartamentoService.buscarOuFalhar(id);
		return ResponseEntity.ok(apartamentoMapper.toModel(apartamento));
	}

	@PostMapping
	@Override
	public ResponseEntity<ApartamentoModel> criar(@RequestBody @Valid ApartamentoInput apartamentoInput) {
		Apartamento apartamento = apartamentoMapper.toDomain(apartamentoInput);
		apartamento = cadastroApartamentoService.criar(apartamento, apartamentoInput.predioId());

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(apartamento.getId())
				.toUri();

		return ResponseEntity.created(location).body(apartamentoMapper.toModel(apartamento));
	}

	@PutMapping("/{id}")
	@Override
	public ResponseEntity<ApartamentoModel> atualizar(
			@PathVariable Long id,
			@RequestBody @Valid ApartamentoInput apartamentoInput) {
		Apartamento dados = apartamentoMapper.toDomain(apartamentoInput);
		Apartamento apartamento = cadastroApartamentoService.atualizar(id, dados, apartamentoInput.predioId());
		return ResponseEntity.ok(apartamentoMapper.toModel(apartamento));
	}

	@DeleteMapping("/{id}")
	@Override
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		cadastroApartamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

}
