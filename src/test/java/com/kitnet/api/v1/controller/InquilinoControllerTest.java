package com.kitnet.api.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.config.IntegrationTest;

class InquilinoControllerTest extends IntegrationTest {

	private InquilinoModel criarInquilino(String nome, String nomeAbreviado, String cpf) {
		InquilinoInput input = new InquilinoInput(nome, nomeAbreviado, null, null, cpf, null);
		return restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(InquilinoModel.class)
			.returnResult()
			.getResponseBody();
	}

	@Test
	void deveCriarInquilinoComSucesso() {
		InquilinoInput input = new InquilinoInput("Fulano da Silva", "Fulano", "fulano@email.com", "11999998888", null, null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.nome").isEqualTo("Fulano da Silva")
			.jsonPath("$.nomeAbreviado").isEqualTo("Fulano")
			.jsonPath("$.email").isEqualTo("fulano@email.com")
			.jsonPath("$.contato").isEqualTo("11999998888")
			.jsonPath("$.status").isEqualTo("ATIVO");
	}

	@Test
	void deveRetornarCriadoComStatus201EHeaderLocation() {
		InquilinoInput input = new InquilinoInput("Beltrano Souza", "Beltrano", null, null, null, null);

		EntityExchangeResult<InquilinoModel> resultado = restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(InquilinoModel.class)
			.returnResult();

		InquilinoModel criado = resultado.getResponseBody();
		String location = resultado.getResponseHeaders().getFirst(HttpHeaders.LOCATION);

		assertThat(location).isNotNull().endsWith("/v1/inquilinos/" + criado.id());
	}

	@Test
	void deveEncontrarInquilinoPorId() {
		InquilinoModel criado = criarInquilino("Ciclano Pereira", "Ciclano", null);

		restTestClient.get().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.nome").isEqualTo("Ciclano Pereira");
	}

	@Test
	void deveRetornar404QuandoInquilinoNaoEncontrado() {
		restTestClient.get().uri("/v1/inquilinos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveAtualizarInquilino() {
		InquilinoModel criado = criarInquilino("Nome Original", "Original", null);

		InquilinoInput atualizado = new InquilinoInput("Nome Renovado", "Renovado", "novo@email.com", "11988887777", null, null);

		restTestClient.put().uri("/v1/inquilinos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizado)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isEqualTo(criado.id())
			.jsonPath("$.nome").isEqualTo("Nome Renovado")
			.jsonPath("$.email").isEqualTo("novo@email.com");
	}

	@Test
	void devePutEmIdInexistenteRetornar404() {
		InquilinoInput input = new InquilinoInput("Inexistente", "Inexist", null, null, null, null);

		restTestClient.put().uri("/v1/inquilinos/9999")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveDeletarInquilino() {
		InquilinoModel criado = criarInquilino("Para Deletar", "Deletar", null);

		restTestClient.delete().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void deveDeleteEmIdInexistenteRetornar404() {
		restTestClient.delete().uri("/v1/inquilinos/9999")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveListarInquilinosPaginado() {
		criarInquilino("Listagem Um", "Um", null);
		criarInquilino("Listagem Dois", "Dois", null);

		restTestClient.get().uri("/v1/inquilinos")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content").isArray()
			.jsonPath("$.content.length()").isEqualTo(2)
			.jsonPath("$.page.totalElements").isEqualTo(2);
	}

	@Test
	void deveListarInquilinosFiltradoPorStatus() {
		InquilinoModel ativo = criarInquilino("Ativo Um", "AtivoUm", null);
		InquilinoModel paraInativar = criarInquilino("Inativo Um", "InativoUm", null);

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", paraInativar.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/inquilinos?status=ATIVO")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.content.length()").isEqualTo(1)
			.jsonPath("$.content[0].id").isEqualTo(ativo.id());
	}

	@Test
	void deveRetornarErroValidacaoParaNomeEmBranco() {
		InquilinoInput input = new InquilinoInput("", "Abrev", null, null, null, null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.nome").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaEmailInvalido() {
		InquilinoInput input = new InquilinoInput("Nome Valido", "Abrev", "nao-e-email", null, null, null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.email").exists();
	}

	@Test
	void deveRetornarErroValidacaoParaNomeAbreviadoMaiorQue20() {
		InquilinoInput input = new InquilinoInput("Nome Valido", "Abreviado com mais de vinte caracteres", null, null, null, null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Erro de validação")
			.jsonPath("$.fields.nomeAbreviado").exists();
	}

	@Test
	void deveManterStatusInativoAposAtualizar() {
		InquilinoModel criado = criarInquilino("Status Original", "StatusOrig", null);

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		InquilinoInput atualizado = new InquilinoInput("Status Atualizado", "StatusAtu", null, null, null, null);
		restTestClient.put().uri("/v1/inquilinos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizado)
			.exchange()
			.expectStatus().isOk();

		restTestClient.get().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("INATIVO");
	}

	@Test
	void deveNormalizarEmailParaMinusculo() {
		InquilinoInput input = new InquilinoInput("Email Caixa Alta", "EmailAlta", "  Fulano@Email.COM  ", null, null, null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.email").isEqualTo("fulano@email.com");
	}

	@Test
	void devePutEmIdInexistenteComCpfDuplicadoRetornar404() {
		criarInquilino("Dono do CPF", "DonoCpf", "52998224725");

		InquilinoInput atualizacaoComCpfDuplicado = new InquilinoInput("Outro Nome", "OutroNome", null, null, "52998224725", null);

		restTestClient.put().uri("/v1/inquilinos/9999")
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizacaoComCpfDuplicado)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

	@Test
	void deveArmazenarCpfSomenteComDigitos() {
		InquilinoInput input = new InquilinoInput("Cpf Formatado", "CpfFormatado", null, null, "529.982.247-25", null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.cpf").isEqualTo("52998224725");
	}

	@Test
	void deveRetornarErroParaCpfDuplicadoComFormatacaoDiferente() {
		criarInquilino("Cpf Digitos", "CpfDigitos", "52998224725");

		InquilinoInput duplicado = new InquilinoInput("Cpf Formatado Dup", "CpfFormDup", null, null, "529.982.247-25", null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(duplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("Já existe um inquilino cadastrado com esse CPF");
	}

	@Test
	void deveTratarCpfVazioComoNulo() {
		InquilinoInput input = new InquilinoInput("Cpf Vazio", "CpfVazio", null, null, "", null);

		restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.cpf").isEmpty();
	}

	@Test
	void deveAtualizarInquilinoComMesmoCpf() {
		InquilinoModel criado = criarInquilino("Cpf Mesmo", "CpfMesmo", "52998224725");

		InquilinoInput atualizado = new InquilinoInput("Cpf Mesmo Editado", "CpfMesmoEd", null, null, "529.982.247-25", null);

		restTestClient.put().uri("/v1/inquilinos/{id}", criado.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizado)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.nome").isEqualTo("Cpf Mesmo Editado")
			.jsonPath("$.cpf").isEqualTo("52998224725");
	}

	@Test
	void deveRetornarErroAoAtualizarComCpfDuplicado() {
		criarInquilino("Cpf Dono", "CpfDono", "52998224725");
		InquilinoModel outro = criarInquilino("Outro Inquilino", "OutroInq", "11144477735");

		InquilinoInput atualizacaoComCpfDuplicado = new InquilinoInput("Outro Inquilino", "OutroInq", null, null, "52998224725", null);

		restTestClient.put().uri("/v1/inquilinos/{id}", outro.id())
			.contentType(MediaType.APPLICATION_JSON)
			.body(atualizacaoComCpfDuplicado)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Já existe um inquilino cadastrado com esse CPF");
	}

	@Test
	void deveRetornarErroParaCpfInvalidoSemExporValor() {
		String cpfInvalido = "11111111111";
		InquilinoInput input = new InquilinoInput("Cpf Invalido", "CpfInvalido", null, null, cpfInvalido, null);

		EntityExchangeResult<byte[]> resultado = restTestClient.post().uri("/v1/inquilinos")
			.contentType(MediaType.APPLICATION_JSON)
			.body(input)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.fields.cpf").exists()
			.returnResult();

		String corpo = new String(resultado.getResponseBody());
		assertThat(corpo).doesNotContain(cpfInvalido);
	}

	@Test
	void deveInativarInquilino() {
		InquilinoModel criado = criarInquilino("Para Inativar", "ParaInativar", null);

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("INATIVO");
	}

	@Test
	void deveReativarInquilino() {
		InquilinoModel criado = criarInquilino("Para Reativar", "ParaReativar", null);

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.delete().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.get().uri("/v1/inquilinos/{id}", criado.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("ATIVO");
	}

	@Test
	void deveInativarInquilinoJaInativoSemErro() {
		InquilinoModel criado = criarInquilino("Duas Vezes Inativo", "DuasVezes", null);

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();

		restTestClient.put().uri("/v1/inquilinos/{id}/inativacao", criado.id())
			.exchange()
			.expectStatus().isNoContent();
	}

	@Test
	void deveRetornar404AoInativarInquilinoInexistente() {
		restTestClient.put().uri("/v1/inquilinos/9999/inativacao")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Entidade não encontrada");
	}

}
