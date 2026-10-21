package com.kitnet.api.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

import jakarta.persistence.EntityManager;

import com.kitnet.api.v1.input.ApartamentoInput;
import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.input.LancamentoInput;
import com.kitnet.api.v1.input.LancamentoPagamentoInput;
import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.model.ApartamentoModel;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.api.v1.model.LancamentoModel;
import com.kitnet.api.v1.model.PredioModel;
import com.kitnet.config.IntegrationTest;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.model.Inquilino;
import com.kitnet.domain.model.Lancamento;
import com.kitnet.domain.repository.ApartamentoRepository;
import com.kitnet.domain.repository.InquilinoRepository;
import com.kitnet.domain.repository.LancamentoRepository;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class LancamentoControllerTest extends IntegrationTest {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ApartamentoRepository apartamentoRepository;

	@Autowired
	private InquilinoRepository inquilinoRepository;

	@Autowired
	private LancamentoRepository lancamentoRepository;

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

	private ApartamentoModel criarApartamento(Long predioId, String numero, String valorAluguel) {
		ApartamentoInput input = new ApartamentoInput(numero, "Kitnet " + numero, null, predioId, new BigDecimal(valorAluguel));
		return restTestClient.post().uri("/v1/apartamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(ApartamentoModel.class)
			.returnResult()
			.getResponseBody();
	}

	private InquilinoModel criarInquilino(String nome) {
		InquilinoInput input = new InquilinoInput(nome, nome, null, null, null, null);
		return restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(InquilinoModel.class)
			.returnResult()
			.getResponseBody();
	}

	private LancamentoModel criarLancamento(Long apartamentoId, Long inquilinoId, LocalDate entrada, LocalDate pagamento, BigDecimal valorPago) {
		LancamentoInput input = new LancamentoInput(apartamentoId, inquilinoId, entrada, pagamento, valorPago, null);
		return restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(LancamentoModel.class)
			.returnResult()
			.getResponseBody();
	}

	@Test
	void deveCriarLancamentoComSucesso() {
		PredioModel predio = criarPredio("Prédio Lanç A", "81345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "101", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç A");

		LancamentoInput input = new LancamentoInput(
			apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), new BigDecimal("50.00"), "observação teste");

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.apartamento.id").isEqualTo(apartamento.id())
			.jsonPath("$.inquilino.id").isEqualTo(inquilino.id())
			.jsonPath("$.status").isEqualTo("ABERTO")
			.jsonPath("$.observacao").isEqualTo("observação teste");
	}

	@Test
	void deveRetornarCriadoComStatus201EHeaderLocation() {
		PredioModel predio = criarPredio("Prédio Lanç B", "82345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "102", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç B");

		LancamentoInput input = new LancamentoInput(
			apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), new BigDecimal("50.00"), null);

		EntityExchangeResult<LancamentoModel> resultado = restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(LancamentoModel.class)
			.returnResult();

		LancamentoModel criado = resultado.getResponseBody();
		String location = resultado.getResponseHeaders().getFirst(HttpHeaders.LOCATION);

		assertThat(location).isNotNull().endsWith("/v1/lancamentos/" + criado.id());
	}

	@Test
	void deveCopiarValorMensalDoApartamento() {
		PredioModel predio = criarPredio("Prédio Lanç C", "83345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "103", "350.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç C");

		LancamentoModel lancamento = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		assertThat(lancamento.valorMensal()).isEqualByComparingTo("350.00");
	}

	@Test
	void deveCalcularValoresConformeExemploDoDominio() {
		PredioModel predio = criarPredio("Prédio Lanç D", "84345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "104", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç D");

		LancamentoModel lancamento = criarLancamento(
			apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), new BigDecimal("50.00"));

		assertThat(lancamento.dias()).isEqualTo(10);
		assertThat(lancamento.valorTotal()).isEqualByComparingTo("90.00");
		assertThat(lancamento.valorPago()).isEqualByComparingTo("50.00");
		assertThat(lancamento.valorDebito()).isEqualByComparingTo("40.00");
		assertThat(lancamento.statusPagamento().name()).isEqualTo("DEBITO");
	}

	@Test
	void deveCobrarValorMensalExatoEmFevereiro() {
		PredioModel predio = criarPredio("Prédio Lanç E", "85345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "105", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç E");

		LancamentoModel lancamento = criarLancamento(
			apartamento.id(), inquilino.id(), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1), null);

		assertThat(lancamento.valorTotal()).isEqualByComparingTo("270.00");
	}

	@Test
	void deveMudarApartamentoParaOcupadoAoCriar() {
		PredioModel predio = criarPredio("Prédio Lanç F", "86345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "106", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç F");

		criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/apartamentos/{id}", apartamento.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("OCUPADO");
	}

	@Test
	void deveCriarLancamentoSemValorPagoComSucesso() {
		PredioModel predio = criarPredio("Prédio Lanç G", "87345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "107", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç G");

		LancamentoModel lancamento = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		assertThat(lancamento.valorPago()).isEqualByComparingTo("0.00");
		assertThat(lancamento.statusPagamento().name()).isEqualTo("DEBITO");
	}

	@Test
	void deveRetornarErro400QuandoApartamentoNaoExiste() {
		InquilinoModel inquilino = criarInquilino("Fulano Lanç H");
		LancamentoInput input = new LancamentoInput(9999L, inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio");
	}

	@Test
	void deveRetornarErro400QuandoInquilinoNaoExiste() {
		PredioModel predio = criarPredio("Prédio Lanç I", "88345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "108", "270.00");
		LancamentoInput input = new LancamentoInput(apartamento.id(), 9999L, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Violação de regra de negócio");
	}

	@Test
	void deveRetornarErro400QuandoApartamentoNaoDisponivel() {
		PredioModel predio = criarPredio("Prédio Lanç J", "89345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "109", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç J1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç J2");

		criarLancamento(apartamento.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoInput input = new LancamentoInput(apartamento.id(), inquilino2.id(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Apartamento não está disponível para locação");
	}

	@Test
	void deveRetornarErro400QuandoInquilinoInativo() {
		PredioModel predio = criarPredio("Prédio Lanç K", "91345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "110", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç K");

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", inquilino.id())
			.exchange()
			.expectStatus().isNoContent();

		LancamentoInput input = new LancamentoInput(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Inquilino inativo não pode receber novo lançamento");
	}

	@Test
	void deveRetornarErro400QuandoDatasIguais() {
		PredioModel predio = criarPredio("Prédio Lanç L", "92345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "111", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç L");

		LancamentoInput input = new LancamentoInput(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 1), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Data de pagamento deve ser posterior à data de entrada");
	}

	@Test
	void deveRetornarErro400QuandoPeriodoMaiorQueUmCiclo() {
		PredioModel predio = criarPredio("Prédio Lanç M", "93345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "112", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç M");

		LancamentoInput input = new LancamentoInput(apartamento.id(), inquilino.id(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 2), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Período não pode ser maior que um ciclo mensal");
	}

	@Test
	void deveRetornarErro400QuandoApartamentoJaTemLancamentoAberto() {
		PredioModel predio = criarPredio("Prédio Lanç N", "94345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "113", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç N1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç N2");

		criarLancamento(apartamento.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoInput input = new LancamentoInput(apartamento.id(), inquilino2.id(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400);
	}

	@Test
	void deveRetornarErro400QuandoInquilinoJaTemLancamentoAberto() {
		PredioModel predio = criarPredio("Prédio Lanç O", "95345678");
		ApartamentoModel apartamento1 = criarApartamento(predio.id(), "114", "270.00");
		ApartamentoModel apartamento2 = criarApartamento(predio.id(), "115", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç O");

		criarLancamento(apartamento1.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoInput input = new LancamentoInput(apartamento2.id(), inquilino.id(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 11), null, null);

		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Inquilino já possui um lançamento aberto");
	}

	@Test
	void deveBloquearNoBancoSegundoLancamentoAbertoParaMesmoApartamento() {
		PredioModel predio = criarPredio("Prédio Lanç P", "96345678");
		ApartamentoModel apartamentoModel = criarApartamento(predio.id(), "116", "270.00");
		InquilinoModel inquilino1Model = criarInquilino("Fulano Lanç P1");
		InquilinoModel inquilino2Model = criarInquilino("Fulano Lanç P2");

		Apartamento apartamento = apartamentoRepository.findById(apartamentoModel.id()).orElseThrow();
		Inquilino inquilino1 = inquilinoRepository.findById(inquilino1Model.id()).orElseThrow();
		Inquilino inquilino2 = inquilinoRepository.findById(inquilino2Model.id()).orElseThrow();

		Lancamento lancamento1 = new Lancamento();
		lancamento1.setApartamento(apartamento);
		lancamento1.setInquilino(inquilino1);
		lancamento1.setDataEntrada(LocalDate.of(2026, 4, 1));
		lancamento1.setDataPagamento(LocalDate.of(2026, 4, 11));
		lancamento1.setValorMensal(new BigDecimal("270.00"));
		lancamento1.calcularValores();
		lancamentoRepository.saveAndFlush(lancamento1);

		Lancamento lancamento2 = new Lancamento();
		lancamento2.setApartamento(apartamento);
		lancamento2.setInquilino(inquilino2);
		lancamento2.setDataEntrada(LocalDate.of(2026, 5, 1));
		lancamento2.setDataPagamento(LocalDate.of(2026, 5, 11));
		lancamento2.setValorMensal(new BigDecimal("270.00"));
		lancamento2.calcularValores();

		assertThatThrownBy(() -> lancamentoRepository.saveAndFlush(lancamento2))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deveEncontrarLancamentoPorId() {
		PredioModel predio = criarPredio("Prédio Lanç Q", "97345678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "117", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç Q");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/lancamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id());
	}

	@Test
	void deveRetornar404QuandoLancamentoNaoEncontrado() {
		restTestClient.get().uri("/v1/lancamentos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveListarLancamentosPaginado() {
		PredioModel predio = criarPredio("Prédio Lanç R", "98345678");
		ApartamentoModel apartamento1 = criarApartamento(predio.id(), "118", "270.00");
		ApartamentoModel apartamento2 = criarApartamento(predio.id(), "119", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç R1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç R2");

		criarLancamento(apartamento1.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);
		criarLancamento(apartamento2.id(), inquilino2.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/lancamentos")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(2)
			.jsonPath("$.page.totalElements").isEqualTo(2);
	}

	@Test
	void deveListarLancamentosFiltradoPorApartamento() {
		PredioModel predio = criarPredio("Prédio Lanç S", "11445678");
		ApartamentoModel apartamento1 = criarApartamento(predio.id(), "120", "270.00");
		ApartamentoModel apartamento2 = criarApartamento(predio.id(), "121", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç S1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç S2");

		LancamentoModel lancamento1 = criarLancamento(apartamento1.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);
		criarLancamento(apartamento2.id(), inquilino2.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/lancamentos?apartamentoId={id}", apartamento1.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1)
			.jsonPath("$.content[0].id").isEqualTo(lancamento1.id());
	}

	@Test
	void deveListarLancamentosFiltradoPorInquilino() {
		PredioModel predio = criarPredio("Prédio Lanç T", "12445678");
		ApartamentoModel apartamento1 = criarApartamento(predio.id(), "122", "270.00");
		ApartamentoModel apartamento2 = criarApartamento(predio.id(), "123", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç T1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç T2");

		LancamentoModel lancamento1 = criarLancamento(apartamento1.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);
		criarLancamento(apartamento2.id(), inquilino2.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/lancamentos?inquilinoId={id}", inquilino1.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1)
			.jsonPath("$.content[0].id").isEqualTo(lancamento1.id());
	}

	@Test
	void deveListarLancamentosFiltradoPorStatus() {
		PredioModel predio = criarPredio("Prédio Lanç U", "13445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "124", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç U");

		criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/lancamentos?status=ABERTO")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1);

		restTestClient.get().uri("/v1/lancamentos?status=FECHADO")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(0);
	}

	@Test
	void deveSubstituirValorPagoNaoSomar() {
		PredioModel predio = criarPredio("Prédio Lanç V", "14445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "125", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç V");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), new BigDecimal("30.00"));
		assertThat(criado.valorDebito()).isEqualByComparingTo("60.00");

		LancamentoPagamentoInput pagamentoInput = new LancamentoPagamentoInput(new BigDecimal("50.00"), null);

		restTestClient.put().uri("/v1/lancamentos/{id}/pagamento", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(pagamentoInput)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.valorPago").isEqualTo(50.00)
			.jsonPath("$.valorDebito").isEqualTo(40.00);
	}

	@Test
	void deveRetornarErroParaValorPagoMaiorQueTotal() {
		PredioModel predio = criarPredio("Prédio Lanç W", "15445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "126", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç W");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoPagamentoInput pagamentoInput = new LancamentoPagamentoInput(new BigDecimal("999.00"), null);

		restTestClient.put().uri("/v1/lancamentos/{id}/pagamento", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(pagamentoInput)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Valor pago não pode ser maior que o valor total do período");
	}

	@Test
	void deveRetornarErroParaValorPagoNegativo() {
		PredioModel predio = criarPredio("Prédio Lanç X", "16445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "127", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç X");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoPagamentoInput pagamentoInput = new LancamentoPagamentoInput(new BigDecimal("-10.00"), null);

		restTestClient.put().uri("/v1/lancamentos/{id}/pagamento", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(pagamentoInput)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.valorPago").exists();
	}

	@Test
	void devePutPagamentoEmIdInexistenteRetornar404() {
		LancamentoPagamentoInput pagamentoInput = new LancamentoPagamentoInput(new BigDecimal("10.00"), null);

		restTestClient.put().uri("/v1/lancamentos/9999/pagamento")
			.contentType(MediaType.APPLICATION_JSON)
			.body(pagamentoInput)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveManterApartamentoEInquilinoImutaveisAoRegistrarPagamento() {
		PredioModel predio = criarPredio("Prédio Lanç Y", "17445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "128", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç Y");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		LancamentoPagamentoInput pagamentoInput = new LancamentoPagamentoInput(new BigDecimal("30.00"), "pagamento parcial");

		restTestClient.put().uri("/v1/lancamentos/{id}/pagamento", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(pagamentoInput)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.apartamento.id").isEqualTo(apartamento.id())
			.jsonPath("$.inquilino.id").isEqualTo(inquilino.id())
			.jsonPath("$.observacao").isEqualTo("pagamento parcial");
	}

	@Test
	void deveNaoAlterarLancamentoAoReajustarApartamentoDepoisDeCriado() {
		PredioModel predio = criarPredio("Prédio Lanç Z", "18445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "129", "270.00");
		InquilinoModel inquilino = criarInquilino("Fulano Lanç Z");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.put().uri("/v1/apartamentos/{id}", apartamento.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ApartamentoInput(apartamento.numero(), apartamento.descricao(), apartamento.medidor(), predio.id(), new BigDecimal("999.00")))
			.exchange()
			.expectStatus().isOk();

		restTestClient.get().uri("/v1/lancamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.valorMensal").isEqualTo(270.00);
	}

	@Test
	void deveNaoExporCpfDoInquilinoNoLancamento() {
		PredioModel predio = criarPredio("Prédio Lanç AA", "19445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "130", "270.00");
		InquilinoInput inquilinoInput = new InquilinoInput("Fulano Lanç AA", "FulanoAA", null, null, "52998224725", null);
		InquilinoModel inquilino = restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(inquilinoInput)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(InquilinoModel.class)
			.returnResult()
			.getResponseBody();

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		EntityExchangeResult<byte[]> resultado = restTestClient.get().uri("/v1/lancamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.returnResult();

		String corpo = new String(resultado.getResponseBody());
		assertThat(corpo).doesNotContain("52998224725").doesNotContain("cpf");
	}

	@Test
	void deveLiberarApartamentoAoExcluirLancamento() {
		PredioModel predio = criarPredio("Prédio Lanç AB", "21445678");
		ApartamentoModel apartamento = criarApartamento(predio.id(), "131", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç AB1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç AB2");

		LancamentoModel criado = criarLancamento(apartamento.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		restTestClient.get().uri("/v1/apartamentos/{id}", apartamento.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("OCUPADO");

		restTestClient.delete().uri("/v1/lancamentos/{id}", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/apartamentos/{id}", apartamento.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("DISPONIVEL");

		LancamentoInput novoInput = new LancamentoInput(apartamento.id(), inquilino2.id(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 11), null, null);
		restTestClient.post().uri("/v1/lancamentos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(novoInput)
			.exchange()
			.expectStatus().isCreated();
	}

	@Test
	void deveRetornar404AoExcluirLancamentoInexistente() {
		restTestClient.delete().uri("/v1/lancamentos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void naoDeveGerarConsultaExtraNaListagem() {
		PredioModel predio = criarPredio("Prédio Lanç AC", "22445678");
		ApartamentoModel apartamento1 = criarApartamento(predio.id(), "132", "270.00");
		ApartamentoModel apartamento2 = criarApartamento(predio.id(), "133", "270.00");
		ApartamentoModel apartamento3 = criarApartamento(predio.id(), "134", "270.00");
		InquilinoModel inquilino1 = criarInquilino("Fulano Lanç AC1");
		InquilinoModel inquilino2 = criarInquilino("Fulano Lanç AC2");
		InquilinoModel inquilino3 = criarInquilino("Fulano Lanç AC3");

		criarLancamento(apartamento1.id(), inquilino1.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);
		criarLancamento(apartamento2.id(), inquilino2.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);
		criarLancamento(apartamento3.id(), inquilino3.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 11), null);

		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();

		restTestClient.get().uri("/v1/lancamentos")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(3);

		long prepareStatementCount = statistics.getPrepareStatementCount();
		assertThat(prepareStatementCount)
			.as("Listagem não deve disparar consulta extra de apartamento/inquilino por lançamento (N+1)")
			.isLessThanOrEqualTo(2);
	}

}
