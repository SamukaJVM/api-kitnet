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

import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.domain.model.StatusInquilino;

@Tag(name = "Inquilinos", description = "Gerenciamento de inquilinos")
public interface InquilinoControllerOpenApi {

	@Operation(summary = "Listar inquilinos", description = "Retorna uma lista paginada de inquilinos, com filtro opcional por status")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Inquilinos listados com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	Page<InquilinoModel> listar(
		@Parameter(description = "Filtra apenas inquilinos com este status") @RequestParam(required = false) @Nullable StatusInquilino status,
		Pageable pageable);

	@Operation(summary = "Buscar inquilino por ID", description = "Retorna os detalhes de um inquilino específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Inquilino encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoModel.class))),
		@ApiResponse(responseCode = "404", description = "Inquilino não encontrado")
	})
	ResponseEntity<InquilinoModel> buscar(@PathVariable Long id);

	@Operation(summary = "Criar novo inquilino", description = "Cria um novo inquilino e retorna o recurso criado")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Inquilino criado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF duplicado")
	})
	ResponseEntity<InquilinoModel> criar(@RequestBody @Valid InquilinoInput inquilinoInput);

	@Operation(summary = "Atualizar inquilino", description = "Atualiza os dados de um inquilino existente (não altera o status)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Inquilino atualizado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = InquilinoModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF duplicado"),
		@ApiResponse(responseCode = "404", description = "Inquilino não encontrado")
	})
	ResponseEntity<InquilinoModel> atualizar(@PathVariable Long id, @RequestBody @Valid InquilinoInput inquilinoInput);

	@Operation(summary = "Deletar inquilino", description = "Remove um inquilino existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Inquilino deletado com sucesso"),
		@ApiResponse(responseCode = "404", description = "Inquilino não encontrado"),
		@ApiResponse(responseCode = "409", description = "Inquilino possui lançamentos e não pode ser deletado")
	})
	ResponseEntity<Void> deletar(@PathVariable Long id);

	@Operation(summary = "Inativar inquilino", description = "Marca o inquilino como INATIVO (idempotente)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Inquilino inativado com sucesso"),
		@ApiResponse(responseCode = "404", description = "Inquilino não encontrado")
	})
	ResponseEntity<Void> inativar(@PathVariable Long id);

	@Operation(summary = "Reativar inquilino", description = "Marca o inquilino como ATIVO (idempotente)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Inquilino reativado com sucesso"),
		@ApiResponse(responseCode = "404", description = "Inquilino não encontrado")
	})
	ResponseEntity<Void> reativar(@PathVariable Long id);

}
