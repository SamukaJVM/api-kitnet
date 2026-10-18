package com.kitnet.api.v1.controller;

import java.net.URI;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.mapper.PredioMapper;
import com.kitnet.api.v1.model.PredioModel;
import com.kitnet.api.v1.openapi.PredioControllerOpenApi;
import com.kitnet.domain.model.Predio;
import com.kitnet.domain.repository.PredioRepository;
import com.kitnet.domain.service.CadastroPredioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/predios")
public class PredioController implements PredioControllerOpenApi {

	private final PredioRepository predioRepository;
	private final CadastroPredioService cadastroPredioService;
	private final PredioMapper predioMapper;

	public PredioController(
			PredioRepository predioRepository,
			CadastroPredioService cadastroPredioService,
			PredioMapper predioMapper) {
		this.predioRepository = predioRepository;
		this.cadastroPredioService = cadastroPredioService;
		this.predioMapper = predioMapper;
	}

	@GetMapping
	@Override
	public Page<PredioModel> listar(Pageable pageable) {
		return predioRepository.findAll(pageable)
				.map(predioMapper::toModel);
	}

	@GetMapping("/{id}")
	@Override
	public ResponseEntity<PredioModel> buscar(@PathVariable Long id) {
		Predio predio = cadastroPredioService.buscarOuFalhar(id);
		return ResponseEntity.ok(predioMapper.toModel(predio));
	}

	@PostMapping
	@Override
	public ResponseEntity<PredioModel> criar(@RequestBody @Valid PredioInput predioInput) {
		Predio predio = predioMapper.toDomain(predioInput);
		predio = cadastroPredioService.salvar(predio);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(predio.getId())
				.toUri();

		return ResponseEntity.created(location).body(predioMapper.toModel(predio));
	}

	@PutMapping("/{id}")
	@Override
	public ResponseEntity<PredioModel> atualizar(
			@PathVariable Long id,
			@RequestBody @Valid PredioInput predioInput) {
		Predio predio = cadastroPredioService.buscarOuFalhar(id);

		predio.setDescricao(predioInput.descricao());
		predio.setCep(predioInput.cep());
		predio.setLogradouro(predioInput.logradouro());
		predio.setComplemento(predioInput.complemento());
		predio.setBairro(predioInput.bairro());
		predio.setLocalidade(predioInput.localidade());
		predio.setUf(predioInput.uf());
		predio.setNumero(predioInput.numero());

		predio = cadastroPredioService.salvar(predio);

		return ResponseEntity.ok(predioMapper.toModel(predio));
	}

	@DeleteMapping("/{id}")
	@Override
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		cadastroPredioService.excluir(id);
		return ResponseEntity.noContent().build();
	}

}
