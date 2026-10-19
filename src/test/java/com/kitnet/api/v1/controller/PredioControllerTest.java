package com.kitnet.api.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.model.PredioModel;
import com.kitnet.config.IntegrationTest;

class PredioControllerTest extends IntegrationTest {

	@Test
	void deveCriarPredioComSucesso() {
		PredioInput input = new PredioInput(
			"Prédio A",
			"12345678",
			"Rua A",
			"Apt 101",
			"Centro",
			"São Paulo",
			"SP",
			"100"
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.descricao").isEqualTo("Prédio A")
			.jsonPath("$.cep").isEqualTo("12345678");
	}

	@Test
	void deveRetornarCriadoComStatus201EHeaderLocation() {
		PredioInput input = new PredioInput(
			"Prédio B",
			"87654321",
			"Rua B",
			null,
			null,
			null,
			null,
			null
		);

		EntityExchangeResult<PredioModel> resultado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult();

		PredioModel criado = resultado.getResponseBody();
		String location = resultado.getResponseHeaders().getFirst(HttpHeaders.LOCATION);

		assertThat(location).isNotNull().endsWith("/v1/predios/" + criado.id());
	}

	@Test
	void deveEncontrarPredioPorId() {
		PredioInput input = new PredioInput(
			"Prédio Para Buscar",
			"11111111",
			null, null, null, null, null, null
		);

		PredioModel criado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();

		restTestClient.get().uri("/v1/predios/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.descricao").isEqualTo("Prédio Para Buscar");
	}

	@Test
	void deveRetornar404QuandoPredioNaoEncontrado() {
		restTestClient.get().uri("/v1/predios/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveRetornarErroParaDescricaoDuplicada() {
		PredioInput input = new PredioInput(
			"Prédio Duplicado",
			"22222222",
			null, null, null, null, null, null
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio")
			.jsonPath("$.detail").isEqualTo("Já existe um prédio cadastrado com essa descrição");
	}

	@Test
	void deveAtualizarPredio() {
		PredioInput input = new PredioInput(
			"Prédio Original",
			"33333333",
			"Rua Original",
			null, null, null, null, null
		);

		PredioModel criado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();

		PredioInput updatedInput = new PredioInput(
			"Prédio Atualizado",
			"44444444",
			"Rua Nova",
			null, null, null, null, null
		);

		restTestClient.put().uri("/v1/predios/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(updatedInput)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.descricao").isEqualTo("Prédio Atualizado")
			.jsonPath("$.logradouro").isEqualTo("Rua Nova");
	}

	@Test
	void deveDeletarPredio() {
		PredioInput input = new PredioInput(
			"Prédio Para Deletar",
			"55555555",
			null, null, null, null, null, null
		);

		PredioModel criado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();

		restTestClient.delete().uri("/v1/predios/{id}", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/predios/{id}", criado.id())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void deveListarPrediosPaginado() {
		PredioInput input1 = new PredioInput(
			"Prédio Para Listar 1",
			"66666666",
			null, null, null, null, null, null
		);
		PredioInput input2 = new PredioInput(
			"Prédio Para Listar 2",
			"66666667",
			null, null, null, null, null, null
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.get().uri("/v1/predios")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content").isArray()
			.jsonPath("$.content.length()").isEqualTo(2)
			.jsonPath("$.page.totalElements").isEqualTo(2);
	}

	@Test
	void deveAtualizarPredioComMesmaDescricao() {
		PredioInput input = new PredioInput(
			"Prédio Mesmo Nome",
			"77777777",
			"Rua Inicial",
			null, null, null, null, null
		);

		PredioModel criado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();

		PredioInput sameDescInput = new PredioInput(
			"Prédio Mesmo Nome",
			"88888888",
			"Rua Alterada",
			null, null, null, null, null
		);

		restTestClient.put().uri("/v1/predios/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(sameDescInput)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.descricao").isEqualTo("Prédio Mesmo Nome")
			.jsonPath("$.cep").isEqualTo("88888888")
			.jsonPath("$.logradouro").isEqualTo("Rua Alterada");
	}

	@Test
	void deveRetornarErroAoAtualizarComDescricaoDuplicada() {
		PredioInput input1 = new PredioInput(
			"Prédio A Para Update",
			"99999999",
			null, null, null, null, null, null
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		PredioInput input2 = new PredioInput(
			"Prédio B Para Update",
			"00000000",
			null, null, null, null, null, null
		);

		PredioModel criado = restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();

		PredioInput updateWithDuplicateDesc = new PredioInput(
			"Prédio A Para Update",
			"11111111",
			null, null, null, null, null, null
		);

		restTestClient.put().uri("/v1/predios/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(updateWithDuplicateDesc)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio")
			.jsonPath("$.detail").isEqualTo("Já existe um prédio cadastrado com essa descrição");
	}

	@Test
	void deveRetornarErroValidacaoComFieldsParaCepInvalido() {
		PredioInput input = new PredioInput(
			"Prédio Validação CEP",
			"123",
			null, null, null, null, null, null
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.cep").exists();
	}

	@Test
	void devePutEmIdInexistenteRetornar404() {
		PredioInput input = new PredioInput(
			"Prédio Inexistente",
			"44444444",
			null, null, null, null, null, null
		);

		restTestClient.put().uri("/v1/predios/9999")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveDeleteEmIdInexistenteRetornar404() {
		restTestClient.delete().uri("/v1/predios/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveRetornarErroValidacaoParaDescricaoEmBranco() {
		PredioInput input = new PredioInput(
			"",
			"12345678",
			null, null, null, null, null, null
		);

		restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.descricao").exists();
	}

}
