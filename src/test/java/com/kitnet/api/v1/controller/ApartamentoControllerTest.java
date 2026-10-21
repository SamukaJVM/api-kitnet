package com.kitnet.api.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;

import com.kitnet.api.v1.input.ApartamentoInput;
import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.input.LancamentoInput;
import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.model.ApartamentoModel;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.api.v1.model.PredioModel;
import com.kitnet.config.IntegrationTest;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ApartamentoControllerTest extends IntegrationTest {

	@Autowired
	private EntityManager entityManager;

	private PredioModel criarPredio(String descricao, String cep) {
		PredioInput input = new PredioInput(descricao, cep, null, null, null, null, null, null);
		return restTestClient.post().uri("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(PredioModel.class)
			.returnResult()
			.getResponseBody();
	}

	private static BigDecimal valor(String valor) {
		return new BigDecimal(valor);
	}

	@Test
	void deveCriarApartamentoComSucesso() {
		PredioModel predio = criarPredio("Prédio Apto A", "12345678");
		ApartamentoInput input = new ApartamentoInput("101", "Kitnet térrea", "MED-1", predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.numero").isEqualTo("101")
			.jsonPath("$.descricao").isEqualTo("Kitnet térrea")
			.jsonPath("$.medidor").isEqualTo("MED-1")
			.jsonPath("$.status").isEqualTo("DISPONIVEL")
			.jsonPath("$.valorAluguel").isEqualTo(270.00)
			.jsonPath("$.predio.id").isEqualTo(predio.id())
			.jsonPath("$.predio.descricao").isEqualTo("Prédio Apto A");
	}

	@Test
	void deveRetornarCriadoComStatus201EHeaderLocation() {
		PredioModel predio = criarPredio("Prédio Apto B", "22345678");
		ApartamentoInput input = new ApartamentoInput("102", "Kitnet fundos", null, predio.id(), valor("270.00"));

		EntityExchangeResult<ApartamentoModel> resultado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult();

		ApartamentoModel criado = resultado.getResponseBody();
		String location = resultado.getResponseHeaders().getFirst(HttpHeaders.LOCATION);

		assertThat(location).isNotNull().endsWith("/v1/apartamentos/" + criado.id());
	}

	@Test
	void deveEncontrarApartamentoPorId() {
		PredioModel predio = criarPredio("Prédio Apto C", "32345678");
		ApartamentoInput input = new ApartamentoInput("103", "Kitnet para buscar", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		restTestClient.get().uri("/v1/apartamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.numero").isEqualTo("103")
			.jsonPath("$.predio.id").isEqualTo(predio.id())
			.jsonPath("$.predio.descricao").isEqualTo("Prédio Apto C");
	}

	@Test
	void deveRetornar404QuandoApartamentoNaoEncontrado() {
		restTestClient.get().uri("/v1/apartamentos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveRetornarErro400QuandoPredioDoApartamentoNaoExiste() {
		ApartamentoInput input = new ApartamentoInput("104", "Kitnet órfã", null, 9999L, valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio");
	}

	@Test
	void deveRetornarErroParaNumeroDuplicadoNoMesmoPredio() {
		PredioModel predio = criarPredio("Prédio Apto D", "42345678");
		ApartamentoInput input = new ApartamentoInput("105", "Kitnet 1", null, predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated();

		ApartamentoInput duplicado = new ApartamentoInput("105", "Kitnet 2", null, predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(duplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio")
			.jsonPath("$.detail").isEqualTo("Já existe um apartamento com esse número neste prédio");
	}

	@Test
	void deveCriarApartamentoComMesmoNumeroEmPrediosDiferentes() {
		PredioModel predio1 = criarPredio("Prédio Apto E1", "52345678");
		PredioModel predio2 = criarPredio("Prédio Apto E2", "52345679");

		ApartamentoInput input1 = new ApartamentoInput("201", "Kitnet prédio 1", null, predio1.id(), valor("270.00"));
		ApartamentoInput input2 = new ApartamentoInput("201", "Kitnet prédio 2", null, predio2.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated();
	}

	@Test
	void deveCriarApartamentosSemMedidorComSucesso() {
		PredioModel predio = criarPredio("Prédio Apto F", "62345678");

		ApartamentoInput semMedidorNull = new ApartamentoInput("301", "Kitnet sem medidor 1", null, predio.id(), valor("270.00"));
		ApartamentoInput semMedidorVazio = new ApartamentoInput("302", "Kitnet sem medidor 2", "", predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(semMedidorNull)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(semMedidorVazio)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.medidor").isEmpty();
	}

	@Test
	void deveRetornarErroParaMedidorDuplicado() {
		PredioModel predio = criarPredio("Prédio Apto G", "72345678");
		ApartamentoInput input = new ApartamentoInput("401", "Kitnet com medidor", "MED-DUP", predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated();

		ApartamentoInput duplicado = new ApartamentoInput("402", "Outra kitnet", "MED-DUP", predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(duplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Já existe um apartamento cadastrado com esse medidor");
	}

	@Test
	void deveAtualizarApartamento() {
		PredioModel predio = criarPredio("Prédio Apto H", "82345678");
		ApartamentoInput input = new ApartamentoInput("501", "Kitnet original", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput atualizado = new ApartamentoInput("502", "Kitnet renovada", "MED-UPD", predio.id(), valor("300.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizado)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.numero").isEqualTo("502")
			.jsonPath("$.descricao").isEqualTo("Kitnet renovada")
			.jsonPath("$.medidor").isEqualTo("MED-UPD")
			.jsonPath("$.valorAluguel").isEqualTo(300.00);
	}

	@Test
	void deveManterStatusDisponivelAposAtualizar() {
		PredioModel predio = criarPredio("Prédio Apto I", "92345678");
		ApartamentoInput input = new ApartamentoInput("601", "Kitnet status", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput atualizado = new ApartamentoInput("602", "Kitnet status atualizada", null, predio.id(), valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizado)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("DISPONIVEL");
	}

	@Test
	void deveAtualizarApartamentoComMesmoNumero() {
		PredioModel predio = criarPredio("Prédio Apto J", "13345678");
		ApartamentoInput input = new ApartamentoInput("701", "Kitnet mesmo número", "MED-SAME", predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput mesmoNumero = new ApartamentoInput("701", "Kitnet mesmo número editada", "MED-SAME", predio.id(), valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(mesmoNumero)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.descricao").isEqualTo("Kitnet mesmo número editada");
	}

	@Test
	void deveRetornarErroAoAtualizarComNumeroDuplicado() {
		PredioModel predio = criarPredio("Prédio Apto K", "14345678");
		ApartamentoInput input1 = new ApartamentoInput("801", "Kitnet A", null, predio.id(), valor("270.00"));
		ApartamentoInput input2 = new ApartamentoInput("802", "Kitnet B", null, predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		ApartamentoModel criado2 = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput atualizacaoComNumeroDuplicado = new ApartamentoInput("801", "Kitnet B renomeada", null, predio.id(), valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado2.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizacaoComNumeroDuplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Já existe um apartamento com esse número neste prédio");
	}

	@Test
	void deveRetornarErroAoAtualizarComMedidorDuplicado() {
		PredioModel predio = criarPredio("Prédio Apto L", "15345678");
		ApartamentoInput input1 = new ApartamentoInput("901", "Kitnet A", "MED-901", predio.id(), valor("270.00"));
		ApartamentoInput input2 = new ApartamentoInput("902", "Kitnet B", "MED-902", predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		ApartamentoModel criado2 = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput atualizacaoComMedidorDuplicado = new ApartamentoInput("902", "Kitnet B", "MED-901", predio.id(), valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado2.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizacaoComMedidorDuplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Já existe um apartamento cadastrado com esse medidor");
	}

	@Test
	void deveRetornarErro400AoAtualizarComPredioInexistente() {
		PredioModel predio = criarPredio("Prédio Apto M", "16345678");
		ApartamentoInput input = new ApartamentoInput("1001", "Kitnet para atualizar", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput atualizacaoComPredioInexistente = new ApartamentoInput("1001", "Kitnet para atualizar", null, 9999L, valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizacaoComPredioInexistente)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio");
	}

	@Test
	void devePutEmIdInexistenteRetornar404() {
		PredioModel predio = criarPredio("Prédio Apto N", "17345678");
		ApartamentoInput input = new ApartamentoInput("1101", "Kitnet inexistente", null, predio.id(), valor("270.00"));

		restTestClient.put().uri("/v1/apartamentos/9999")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveDeletarApartamento() {
		PredioModel predio = criarPredio("Prédio Apto O", "18345678");
		ApartamentoInput input = new ApartamentoInput("1201", "Kitnet para deletar", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		restTestClient.delete().uri("/v1/apartamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/apartamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void deveDeleteEmIdInexistenteRetornar404() {
		restTestClient.delete().uri("/v1/apartamentos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveListarApartamentosPaginado() {
		PredioModel predio = criarPredio("Prédio Apto P", "19345678");
		ApartamentoInput input1 = new ApartamentoInput("1301", "Kitnet listagem 1", null, predio.id(), valor("270.00"));
		ApartamentoInput input2 = new ApartamentoInput("1302", "Kitnet listagem 2", null, predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.get().uri("/v1/apartamentos")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content").isArray()
			.jsonPath("$.content.length()").isEqualTo(2)
			.jsonPath("$.page.totalElements").isEqualTo(2);
	}

	@Test
	void deveListarApartamentosFiltradoPorPredioId() {
		PredioModel predio1 = criarPredio("Prédio Apto Q1", "21345678");
		PredioModel predio2 = criarPredio("Prédio Apto Q2", "21345679");

		ApartamentoInput input1 = new ApartamentoInput("1401", "Kitnet prédio 1", null, predio1.id(), valor("270.00"));
		ApartamentoInput input2 = new ApartamentoInput("1402", "Kitnet prédio 2", null, predio2.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input1)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input2)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.get().uri("/v1/apartamentos?predioId={predioId}", predio1.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1)
			.jsonPath("$.content[0].numero").isEqualTo("1401")
			.jsonPath("$.page.totalElements").isEqualTo(1);
	}

	@Test
	void deveRetornarErroValidacaoParaNumeroEmBranco() {
		PredioModel predio = criarPredio("Prédio Apto R", "23345678");
		ApartamentoInput input = new ApartamentoInput("", "Kitnet sem número", null, predio.id(), valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.numero").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaPredioIdNulo() {
		ApartamentoInput input = new ApartamentoInput("1501", "Kitnet sem prédio", null, null, valor("270.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.predioId").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaValorAluguelObrigatorio() {
		PredioModel predio = criarPredio("Prédio Apto S", "25345678");
		ApartamentoInput input = new ApartamentoInput("1502", "Kitnet sem valor", null, predio.id(), null);

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.valorAluguel").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaValorAluguelZero() {
		PredioModel predio = criarPredio("Prédio Apto T", "26345678");
		ApartamentoInput input = new ApartamentoInput("1503", "Kitnet valor zero", null, predio.id(), valor("0.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.fields.valorAluguel").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaValorAluguelNegativo() {
		PredioModel predio = criarPredio("Prédio Apto U", "27345678");
		ApartamentoInput input = new ApartamentoInput("1504", "Kitnet valor negativo", null, predio.id(), valor("-10.00"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.fields.valorAluguel").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaValorAluguelComMaisDeDuasCasasDecimais() {
		PredioModel predio = criarPredio("Prédio Apto V", "28345678");
		ApartamentoInput input = new ApartamentoInput("1505", "Kitnet valor com 3 decimais", null, predio.id(), valor("270.123"));

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.fields.valorAluguel").exists();
	}

	@Test
	void deveNormalizarValorAluguelComUmaCasaDecimal() {
		PredioModel predio = criarPredio("Prédio Apto W", "29345678");
		ApartamentoInput input = new ApartamentoInput("1506", "Kitnet valor com 1 decimal", null, predio.id(), valor("270.5"));

		EntityExchangeResult<byte[]> criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.returnResult();

		String corpoCriacao = new String(criado.getResponseBody());
		assertThat(corpoCriacao).contains("\"valorAluguel\":270.50");

		Long id = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput("1507", "Kitnet valor com 1 decimal GET", null, predio.id(), valor("270.5")))
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody()
			.id();

		EntityExchangeResult<byte[]> buscado = restTestClient.get().uri("/v1/apartamentos/{id}", id)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.returnResult();

		String corpoBusca = new String(buscado.getResponseBody());
		assertThat(corpoBusca).contains("\"valorAluguel\":270.50");
	}

	@Test
	void deveAtualizarValorAluguelDoApartamento() {
		PredioModel predio = criarPredio("Prédio Apto X", "31345678");
		ApartamentoInput input = new ApartamentoInput("1508", "Kitnet reajuste", null, predio.id(), valor("270.00"));

		ApartamentoModel criado = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		ApartamentoInput reajuste = new ApartamentoInput("1508", "Kitnet reajuste", null, predio.id(), valor("320.00"));

		restTestClient.put().uri("/v1/apartamentos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(reajuste)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.valorAluguel").isEqualTo(320.00);
	}

	@Test
	void naoDeveGerarConsultaExtraPorApartamentoNaListagem() {
		PredioModel predio1 = criarPredio("Prédio Apto N1", "24345678");
		PredioModel predio2 = criarPredio("Prédio Apto N2", "24345679");
		PredioModel predio3 = criarPredio("Prédio Apto N3", "24345680");

		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput("1601", "Kitnet N1", null, predio1.id(), valor("270.00")))
			.exchange()
			.expectStatus().isCreated();
		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput("1602", "Kitnet N2", null, predio2.id(), valor("270.00")))
			.exchange()
			.expectStatus().isCreated();
		restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput("1603", "Kitnet N3", null, predio3.id(), valor("270.00")))
			.exchange()
			.expectStatus().isCreated();

		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();

		restTestClient.get().uri("/v1/apartamentos")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(3);

		long prepareStatementCount = statistics.getPrepareStatementCount();
		assertThat(prepareStatementCount)
			.as("Listagem não deve disparar uma query extra de prédio por apartamento (N+1)")
			.isLessThanOrEqualTo(2);
	}

	@Test
	void deveRetornar409AoExcluirApartamentoComLancamento() {
		PredioModel predio = criarPredio("Prédio Apto Y", "41345678");
		ApartamentoModel apartamento = restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput("1701", "Kitnet com lançamento", null, predio.id(), valor("270.00")))
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();

		InquilinoModel inquilino = restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new InquilinoInput("Inquilino Bloqueia Apartamento", "Bloqueia", null, null, null, null))
			.exchange()
			.expectStatus().isCreated()
			.expectBody(InquilinoModel.class)
			.returnResult()
			.getResponseBody();

		LancamentoInput lancamentoInput = new LancamentoInput(
			apartamento.id(), inquilino.id(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(lancamentoInput)
			.exchange()
			.expectStatus().isCreated();

		restTestClient.delete().uri("/v1/apartamentos/{id}", apartamento.id())
			.exchange()
			.expectStatus().isEqualTo(409)
			.expectBody()
			.jsonPath("$.status").isEqualTo(409)
			.jsonPath("$.title").isEqualTo("Entidade em uso");
	}

}
