package com.kitnet.api.v1.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record ApartamentoInput(
	@NotBlank(message = "Número é obrigatório")
	@Size(min = 1, max = 10, message = "Número deve ter entre 1 e 10 caracteres")
	String numero,

	@NotBlank(message = "Descrição é obrigatória")
	@Size(min = 1, max = 90, message = "Descrição deve ter entre 1 e 90 caracteres")
	String descricao,

	@Size(max = 10, message = "Medidor não pode exceder 10 caracteres")
	@Nullable
	String medidor,

	@NotNull(message = "Prédio é obrigatório")
	Long predioId
) {
}
