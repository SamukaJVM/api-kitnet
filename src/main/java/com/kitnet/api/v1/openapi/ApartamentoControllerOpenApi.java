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
import com.kitnet.api.v1.input.ApartamentoInput;
import com.kitnet.api.v1.model.ApartamentoModel;

@Tag(name = "Apartamentos", description = "Gerenciamento de apartamentos (kitnets)")
public interface ApartamentoControllerOpenApi {

	@Operation(summary = "Listar apartamentos", description = "Retorna uma lista paginada de apartamentos, com filtro opcional por prédio")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Apartamentos listados com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	Page<ApartamentoModel> listar(
		@Parameter(description = "Filtra apenas apartamentos deste prédio") @RequestParam(required = false) @Nullable Long predioId,
		Pageable pageable);

	@Operation(summary = "Buscar apartamento por ID", description = "Retorna os detalhes de um apartamento específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Apartamento encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApartamentoModel.class))),
		@ApiResponse(responseCode = "404", description = "Apartamento não encontrado")
	})
	ResponseEntity<ApartamentoModel> buscar(@PathVariable Long id);

	@Operation(summary = "Criar novo apartamento", description = "Cria um novo apartamento vinculado a um prédio e retorna o recurso criado")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Apartamento criado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApartamentoModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos, prédio inexistente ou número/medidor duplicado")
	})
	ResponseEntity<ApartamentoModel> criar(@RequestBody @Valid ApartamentoInput apartamentoInput);

	@Operation(summary = "Atualizar apartamento", description = "Atualiza os dados de um apartamento existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Apartamento atualizado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApartamentoModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos, prédio inexistente ou número/medidor duplicado"),
		@ApiResponse(responseCode = "404", description = "Apartamento não encontrado")
	})
	ResponseEntity<ApartamentoModel> atualizar(@PathVariable Long id, @RequestBody @Valid ApartamentoInput apartamentoInput);

	@Operation(summary = "Deletar apartamento", description = "Remove um apartamento existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Apartamento deletado com sucesso"),
		@ApiResponse(responseCode = "404", description = "Apartamento não encontrado"),
		@ApiResponse(responseCode = "409", description = "Apartamento possui lançamentos e não pode ser deletado")
	})
	ResponseEntity<Void> deletar(@PathVariable Long id);

}
