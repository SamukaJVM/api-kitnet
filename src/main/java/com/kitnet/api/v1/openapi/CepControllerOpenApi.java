package com.kitnet.api.v1.openapi;

import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.kitnet.api.v1.model.CepModel;

@Tag(name = "CEPs", description = "Consulta de endereço por CEP")
public interface CepControllerOpenApi {

	@Operation(summary = "Consultar CEP", description = "Consulta o ViaCEP e retorna o endereço correspondente")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Endereço encontrado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = CepModel.class))),
		@ApiResponse(responseCode = "400", description = "CEP fora do formato esperado (8 dígitos numéricos)"),
		@ApiResponse(responseCode = "404", description = "CEP não encontrado"),
		@ApiResponse(responseCode = "503", description = "Serviço de consulta de CEP indisponível")
	})
	CepModel buscar(
			@PathVariable
			@Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
			String cep);

}
