package com.kitnet.infrastructure.viacep;

record ViaCepResponse(
		String cep,
		String logradouro,
		String complemento,
		String bairro,
		String localidade,
		String uf,
		Object erro) {

	boolean indicaErro() {
		return erro != null && (Boolean.TRUE.equals(erro) || "true".equalsIgnoreCase(String.valueOf(erro)));
	}

}
