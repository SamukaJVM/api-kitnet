package com.kitnet.api.v1.input;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
	Long predioId,

	@NotNull(message = "Valor do aluguel é obrigatório")
	@DecimalMin(value = "0.01", message = "Valor do aluguel deve ser maior que zero")
	@Digits(integer = 8, fraction = 2, message = "Valor do aluguel deve ter no máximo 8 dígitos inteiros e 2 decimais")
	BigDecimal valorAluguel
) {

	public ApartamentoInput {
		medidor = normalizarBranco(medidor);
		valorAluguel = normalizarEscala(valorAluguel);
	}

	private static @Nullable String normalizarBranco(@Nullable String valor) {
		if (valor == null) {
			return null;
		}
		String normalizado = valor.trim();
		return normalizado.isBlank() ? null : normalizado;
	}

	// Só preenche zeros à direita (270 -> 270.00); nunca arredonda, para que um valor
	// com mais de 2 casas decimais chegue intacto ao @Digits e seja rejeitado como 400.
	private static @Nullable BigDecimal normalizarEscala(@Nullable BigDecimal valor) {
		if (valor == null || valor.scale() >= 2) {
			return valor;
		}
		return valor.setScale(2);
	}
}
