package com.kitnet.api.v1.model;

import org.jspecify.annotations.Nullable;

import com.kitnet.domain.model.StatusApartamento;

public record ApartamentoModel(
	Long id,
	String numero,
	String descricao,
	@Nullable
	String medidor,
	StatusApartamento status,
	PredioResumoModel predio
) {
}
