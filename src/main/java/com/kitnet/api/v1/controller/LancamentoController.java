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

import com.kitnet.api.v1.input.LancamentoInput;
import com.kitnet.api.v1.input.LancamentoPagamentoInput;
import com.kitnet.api.v1.mapper.LancamentoMapper;
import com.kitnet.api.v1.model.LancamentoModel;
import com.kitnet.api.v1.openapi.LancamentoControllerOpenApi;
import com.kitnet.domain.model.Lancamento;
import com.kitnet.domain.model.StatusLancamento;
import com.kitnet.domain.service.FluxoLancamentoService;

@RestController
@RequestMapping("/v1/lancamentos")
public class LancamentoController implements LancamentoControllerOpenApi {

	private final FluxoLancamentoService fluxoLancamentoService;
	private final LancamentoMapper lancamentoMapper;

	public LancamentoController(
			FluxoLancamentoService fluxoLancamentoService,
			LancamentoMapper lancamentoMapper) {
		this.fluxoLancamentoService = fluxoLancamentoService;
		this.lancamentoMapper = lancamentoMapper;
	}

	@GetMapping
	@Override
	public Page<LancamentoModel> listar(
			@RequestParam(required = false) @Nullable Long apartamentoId,
			@RequestParam(required = false) @Nullable Long inquilinoId,
			@RequestParam(required = false) @Nullable StatusLancamento status,
			Pageable pageable) {
		return fluxoLancamentoService.listar(apartamentoId, inquilinoId, status, pageable)
				.map(lancamentoMapper::toModel);
	}

	@GetMapping("/{id}")
	@Override
	public ResponseEntity<LancamentoModel> buscar(@PathVariable Long id) {
		Lancamento lancamento = fluxoLancamentoService.buscarOuFalhar(id);
		return ResponseEntity.ok(lancamentoMapper.toModel(lancamento));
	}

	@PostMapping
	@Override
	public ResponseEntity<LancamentoModel> criar(@RequestBody @Valid LancamentoInput lancamentoInput) {
		Lancamento lancamento = lancamentoMapper.toDomain(lancamentoInput);
		lancamento = fluxoLancamentoService.criar(lancamento, lancamentoInput.apartamentoId(), lancamentoInput.inquilinoId());

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(lancamento.getId())
				.toUri();

		return ResponseEntity.created(location).body(lancamentoMapper.toModel(lancamento));
	}

	@PutMapping("/{id}/pagamento")
	@Override
	public ResponseEntity<LancamentoModel> registrarPagamento(
			@PathVariable Long id,
			@RequestBody @Valid LancamentoPagamentoInput pagamentoInput) {
		Lancamento lancamento = fluxoLancamentoService.registrarPagamento(id, pagamentoInput.valorPago(), pagamentoInput.observacao());
		return ResponseEntity.ok(lancamentoMapper.toModel(lancamento));
	}

	@DeleteMapping("/{id}")
	@Override
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		fluxoLancamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/fechamento")
	@Override
	public ResponseEntity<Void> fechar(@PathVariable Long id) {
		fluxoLancamentoService.fechar(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/fechamento")
	@Override
	public ResponseEntity<Void> reabrir(@PathVariable Long id) {
		fluxoLancamentoService.reabrir(id);
		return ResponseEntity.noContent().build();
	}

}
