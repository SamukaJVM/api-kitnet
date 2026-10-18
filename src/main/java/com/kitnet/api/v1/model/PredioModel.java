package com.kitnet.api.v1.model;

import org.jspecify.annotations.Nullable;

public record PredioModel(
	Long id,
	String descricao,
	String cep,
	@Nullable
	String logradouro,
	@Nullable
	String complemento,
	@Nullable
	String bairro,
	@Nullable
	String localidade,
	@Nullable
	String uf,
	@Nullable
	String numero
) {
}
