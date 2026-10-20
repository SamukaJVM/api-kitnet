package com.kitnet.api.v1.controller;

import org.junit.jupiter.api.Test;

import com.kitnet.config.IntegrationTest;
import com.kitnet.domain.exception.CepNaoEncontradoException;
import com.kitnet.domain.exception.ServicoExternoIndisponivelException;
import com.kitnet.domain.model.Endereco;

class CepControllerTest extends IntegrationTest {

	@Test
	void deveRetornarEnderecoQuandoCepValido() {
		stubEnderecoCepFinder.retornar(new Endereco(
			"01310200", "Avenida Paulista", "Bela Vista", "São Paulo", "SP"));

		restTestClient.get().uri("/v1/ceps/01310200")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.cep").isEqualTo("01310200")
			.jsonPath("$.logradouro").isEqualTo("Avenida Paulista")
			.jsonPath("$.bairro").isEqualTo("Bela Vista")
			.jsonPath("$.localidade").isEqualTo("São Paulo")
			.jsonPath("$.uf").isEqualTo("SP");
	}

	@Test
	void deveRetornar400QuandoFormatoInvalido() {
		restTestClient.get().uri("/v1/ceps/123")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.cep").exists();
	}

	@Test
	void deveRetornar404QuandoCepNaoEncontrado() {
		stubEnderecoCepFinder.lancar(new CepNaoEncontradoException("CEP não encontrado: 00000000"));

		restTestClient.get().uri("/v1/ceps/00000000")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveRetornar503QuandoViaCepIndisponivel() {
		stubEnderecoCepFinder.lancar(new ServicoExternoIndisponivelException("Serviço de consulta de CEP indisponível"));

		restTestClient.get().uri("/v1/ceps/01310200")
			.exchange()
			.expectStatus().isEqualTo(503)
			.expectBody()
			.jsonPath("$.status").isEqualTo(503)
			.jsonPath("$.title").isEqualTo("Serviço externo indisponível");
	}

}
