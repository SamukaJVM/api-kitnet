package com.kitnet.api.v1.input;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

public record LancamentoInput(
	@NotNull(message = "Apartamento é obrigatório")
	Long apartamentoId,

	@NotNull(message = "Inquilino é obrigatório")
	Long inquilinoId,

	@NotNull(message = "Data de entrada é obrigatória")
	LocalDate dataEntrada,

	@NotNull(message = "Data de pagamento é obrigatória")
	LocalDate dataPagamento,

	@DecimalMin(value = "0.00", message = "Valor pago não pode ser negativo")
	BigDecimal valorPago,

	@Size(max = 255, message = "Observação não pode exceder 255 caracteres")
	@Nullable
	String observacao
) {

	public LancamentoInput {
		valorPago = valorPago == null ? BigDecimal.ZERO : valorPago;
	}
}
