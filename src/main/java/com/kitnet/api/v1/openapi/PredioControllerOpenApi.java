package com.kitnet.api.v1.openapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.model.PredioModel;

@Tag(name = "Prédios", description = "Gerenciamento de prédios")
public interface PredioControllerOpenApi {

	@Operation(summary = "Listar prédios", description = "Retorna uma lista paginada de prédios")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Prédios listados com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	Page<PredioModel> listar(Pageable pageable);

	@Operation(summary = "Buscar prédio por ID", description = "Retorna os detalhes de um prédio específico")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Prédio encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredioModel.class))),
		@ApiResponse(responseCode = "404", description = "Prédio não encontrado")
	})
	ResponseEntity<PredioModel> buscar(@PathVariable Long id);

	@Operation(summary = "Criar novo prédio", description = "Cria um novo prédio e retorna o recurso criado")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Prédio criado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredioModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos ou descrição duplicada"),
		@ApiResponse(responseCode = "409", description = "Conflito ao criar prédio")
	})
	ResponseEntity<PredioModel> criar(@RequestBody @Valid PredioInput predioInput);

	@Operation(summary = "Atualizar prédio", description = "Atualiza os dados de um prédio existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Prédio atualizado com sucesso",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = PredioModel.class))),
		@ApiResponse(responseCode = "400", description = "Dados inválidos"),
		@ApiResponse(responseCode = "404", description = "Prédio não encontrado")
	})
	ResponseEntity<PredioModel> atualizar(@PathVariable Long id, @RequestBody @Valid PredioInput predioInput);

	@Operation(summary = "Deletar prédio", description = "Remove um prédio existente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Prédio deletado com sucesso"),
		@ApiResponse(responseCode = "404", description = "Prédio não encontrado"),
		@ApiResponse(responseCode = "409", description = "Prédio possui apartamentos e não pode ser deletado")
	})
	ResponseEntity<Void> deletar(@PathVariable Long id);

}
