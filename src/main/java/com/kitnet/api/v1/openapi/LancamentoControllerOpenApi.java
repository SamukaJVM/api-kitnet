package com.kitnet.api.v1.openapi;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.kitnet.api.v1.input.LancamentoInput;
import com.kitnet.api.v1.input.LancamentoPagamentoInput;
import com.kitnet.api.v1.model.LancamentoModel;
import com.kitnet.domain.model.StatusLancamento;

@Tag(name = "Lançamentos", description = "Gerenciamento de lançamentos de aluguel")
public interface LancamentoControllerOpenApi {

	@Operation(summary = "Listar lançamentos", description = "Retorna uma lista paginada de lançamentos, com filtros opcionais por apartamento, inquilino e status")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lançamentos listados com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	Page<LancamentoModel> listar(
		@Parameter(description = "Filtra apenas lançamentos deste apartamento") @RequestParam(required = false) @Nullable Long apartamentoId,
		@Parameter(description = "Filtra apenas lançamentos deste inquilino") @RequestParam(required = false) @Nullable Long inquilinoId,
		@Parameter(description = "Filtra apenas lançamentos com este status") @RequestParam(required = false) @Nullable StatusLancamento status,
		Pageable pageable);

	@Operation(summary = "Buscar lançamento por ID", description = "Retorna os detalhes de um lançamento específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Lançamento encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = LancamentoModel.class))),
		@ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
	})
	ResponseEntity<LancamentoModel> buscar(@PathVariable Long id);

	@Operation(summary = "Criar novo lançamento", description = "Cria um novo lançamento de aluguel, copiando o valor do aluguel do apartamento e calculando dias/total/débito")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Lançamento criado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = LancamentoModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos, apartamento/inquilino inexistente ou indisponível, período inválido ou apartamento/inquilino já com lançamento aberto")
	})
	ResponseEntity<LancamentoModel> criar(@RequestBody @Valid LancamentoInput lancamentoInput);

	@Operation(summary = "Registrar pagamento", description = "Substitui o valor total pago até o momento (não soma) e recalcula o débito. Datas, apartamento e inquilino são imutáveis.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = LancamentoModel.class))),
		@ApiResponse(responseCode = "400", description = "Valor pago negativo, maior que o total, ou lançamento fechado"),
		@ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
	})
	ResponseEntity<LancamentoModel> registrarPagamento(@PathVariable Long id, @RequestBody @Valid LancamentoPagamentoInput pagamentoInput);

	@Operation(summary = "Excluir lançamento", description = "Remove um lançamento aberto e libera o apartamento. Lançamentos fechados não podem ser excluídos.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Lançamento excluído com sucesso"),
		@ApiResponse(responseCode = "400", description = "Lançamento fechado não pode ser excluído"),
		@ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
	})
	ResponseEntity<Void> deletar(@PathVariable Long id);

	@Operation(summary = "Fechar lançamento", description = "Fecha o lançamento (só se estiver PAGO) e libera o apartamento. Idempotente: fechar um já fechado não faz nada.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Lançamento fechado com sucesso (ou já estava fechado)"),
		@ApiResponse(responseCode = "400", description = "Lançamento em débito não pode ser fechado"),
		@ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
	})
	ResponseEntity<Void> fechar(@PathVariable Long id);

	@Operation(summary = "Reabrir lançamento", description = "Reabre um lançamento fechado, exigindo apartamento disponível e inquilino ativo sem outro lançamento aberto. Idempotente: reabrir um já aberto não faz nada.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Lançamento reaberto com sucesso (ou já estava aberto)"),
		@ApiResponse(responseCode = "400", description = "Apartamento indisponível, inquilino inativo ou apartamento/inquilino já com outro lançamento aberto"),
		@ApiResponse(responseCode = "404", description = "Lançamento não encontrado")
	})
	ResponseEntity<Void> reabrir(@PathVariable Long id);

}
