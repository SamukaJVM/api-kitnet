package com.kitnet.api.v1.model;

public record CepModel(
		String cep,
		String logradouro,
		String complemento,
		String bairro,
		String localidade,
		String uf) {
}
