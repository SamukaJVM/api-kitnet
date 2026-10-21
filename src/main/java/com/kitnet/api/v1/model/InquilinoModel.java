package com.kitnet.api.v1.model;

import org.jspecify.annotations.Nullable;

import com.kitnet.domain.model.Genero;
import com.kitnet.domain.model.StatusInquilino;

public record InquilinoModel(
	Long id,
	String nome,
	String nomeAbreviado,
	@Nullable
	String email,
	@Nullable
	String contato,
	@Nullable
	String cpf,
	@Nullable
	Genero genero,
	StatusInquilino status
) {
}
