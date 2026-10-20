package com.kitnet.domain.model;

public record Endereco(
		String cep,
		String logradouro,
		String bairro,
		String localidade,
		String uf) {
}
