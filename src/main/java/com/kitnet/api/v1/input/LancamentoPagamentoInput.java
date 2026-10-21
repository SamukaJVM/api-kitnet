package com.kitnet.api.v1.input;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

public record LancamentoPagamentoInput(
	@NotNull(message = "Valor pago é obrigatório")
	@DecimalMin(value = "0.00", message = "Valor pago não pode ser negativo")
	BigDecimal valorPago,

	@Size(max = 255, message = "Observação não pode exceder 255 caracteres")
	@Nullable
	String observacao
) {
}
