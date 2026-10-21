package com.kitnet.api.v1.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import com.kitnet.domain.model.StatusLancamento;
import com.kitnet.domain.model.StatusPagamento;

public record LancamentoModel(
	Long id,
	ApartamentoResumoModel apartamento,
	InquilinoResumoModel inquilino,
	LocalDate dataEntrada,
	LocalDate dataPagamento,
	BigDecimal valorMensal,
	int dias,
	BigDecimal valorTotal,
	BigDecimal valorPago,
	BigDecimal valorDebito,
	StatusLancamento status,
	StatusPagamento statusPagamento,
	@Nullable
	String observacao
) {
}
