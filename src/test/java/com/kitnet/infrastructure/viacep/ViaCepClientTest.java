package com.kitnet.infrastructure.viacep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.kitnet.domain.exception.CepNaoEncontradoException;
import com.kitnet.domain.exception.ServicoExternoIndisponivelException;
import com.kitnet.domain.model.Endereco;

class ViaCepClientTest {

	private ViaCepClient criarClientComMock(MockRestServiceServer[] serverOut) {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		serverOut[0] = server;
		return new ViaCepClient(builder.build());
	}

	@Test
	void deveRetornarEnderecoQuandoCepValido() {
		MockRestServiceServer[] serverOut = new MockRestServiceServer[1];
		ViaCepClient client = criarClientComMock(serverOut);

		serverOut[0].expect(requestTo("https://viacep.com.br/ws/01310200/json"))
			.andRespond(withSuccess(
				"""
				{"cep":"01310-200","logradouro":"Avenida Paulista","complemento":"","bairro":"Bela Vista","localidade":"São Paulo","uf":"SP"}
				""",
				MediaType.APPLICATION_JSON));

		Endereco endereco = client.buscarPorCep("01310200");

		assertThat(endereco.cep()).isEqualTo("01310-200");
		assertThat(endereco.logradouro()).isEqualTo("Avenida Paulista");
		assertThat(endereco.bairro()).isEqualTo("Bela Vista");
		assertThat(endereco.localidade()).isEqualTo("São Paulo");
		assertThat(endereco.uf()).isEqualTo("SP");
	}

	@Test
	void deveLancarCepNaoEncontradoQuandoErroBoolean() {
		MockRestServiceServer[] serverOut = new MockRestServiceServer[1];
		ViaCepClient client = criarClientComMock(serverOut);

		serverOut[0].expect(requestTo("https://viacep.com.br/ws/00000000/json"))
			.andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> client.buscarPorCep("00000000"))
			.isInstanceOf(CepNaoEncontradoException.class);
	}

	@Test
	void deveLancarCepNaoEncontradoQuandoErroString() {
		MockRestServiceServer[] serverOut = new MockRestServiceServer[1];
		ViaCepClient client = criarClientComMock(serverOut);

		serverOut[0].expect(requestTo("https://viacep.com.br/ws/00000001/json"))
			.andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> client.buscarPorCep("00000001"))
			.isInstanceOf(CepNaoEncontradoException.class);
	}

	@Test
	void deveLancarServicoIndisponivelQuandoErro5xx() {
		MockRestServiceServer[] serverOut = new MockRestServiceServer[1];
		ViaCepClient client = criarClientComMock(serverOut);

		serverOut[0].expect(requestTo("https://viacep.com.br/ws/01310200/json"))
			.andRespond(withServerError());

		assertThatThrownBy(() -> client.buscarPorCep("01310200"))
			.isInstanceOf(ServicoExternoIndisponivelException.class);
	}

	@Test
	void deveLancarServicoIndisponivelQuandoFalhaDeConexao() {
		MockRestServiceServer[] serverOut = new MockRestServiceServer[1];
		ViaCepClient client = criarClientComMock(serverOut);

		serverOut[0].expect(requestTo("https://viacep.com.br/ws/01310200/json"))
			.andRespond(request -> {
				throw new IOException("timeout simulado");
			});

		assertThatThrownBy(() -> client.buscarPorCep("01310200"))
			.isInstanceOf(ServicoExternoIndisponivelException.class);
	}

}
