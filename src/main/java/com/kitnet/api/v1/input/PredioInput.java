package com.kitnet.api.v1.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record PredioInput(
	@NotBlank(message = "Descrição é obrigatória")
	@Size(min = 3, max = 90, message = "Descrição deve ter entre 3 e 90 caracteres")
	String descricao,

	@NotBlank(message = "CEP é obrigatório")
	@Pattern(regexp = "^\\d{8}$", message = "CEP deve conter exatamente 8 dígitos")
	String cep,

	@Size(max = 255, message = "Logradouro não pode exceder 255 caracteres")
	@Nullable
	String logradouro,

	@Size(max = 255, message = "Complemento não pode exceder 255 caracteres")
	@Nullable
	String complemento,

	@Size(max = 90, message = "Bairro não pode exceder 90 caracteres")
	@Nullable
	String bairro,

	@Size(max = 90, message = "Localidade não pode exceder 90 caracteres")
	@Nullable
	String localidade,

	@Size(max = 2, message = "UF deve ter exatamente 2 caracteres")
	@Nullable
	String uf,

	@Size(max = 10, message = "Número não pode exceder 10 caracteres")
	@Nullable
	String numero
) {
}
