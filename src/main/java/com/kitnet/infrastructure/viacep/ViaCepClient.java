package com.kitnet.infrastructure.viacep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.kitnet.domain.exception.CepNaoEncontradoException;
import com.kitnet.domain.exception.ServicoExternoIndisponivelException;
import com.kitnet.domain.model.Endereco;
import com.kitnet.domain.service.EnderecoCepFinder;

@Component
public class ViaCepClient implements EnderecoCepFinder {

	private static final Logger log = LoggerFactory.getLogger(ViaCepClient.class);

	private final RestClient restClient;

	public ViaCepClient(@Qualifier("viaCepRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public Endereco buscarPorCep(String cep) {
		ViaCepResponse resposta;
		try {
			resposta = restClient.get()
					.uri("/{cep}/json", cep)
					.retrieve()
					.body(ViaCepResponse.class);
		} catch (RestClientException ex) {
			log.error("Falha ao consultar o ViaCEP para o CEP {}", cep, ex);
			throw new ServicoExternoIndisponivelException("Serviço de consulta de CEP indisponível", ex);
		}

		if (resposta == null || resposta.indicaErro()) {
			throw new CepNaoEncontradoException("CEP não encontrado: " + cep);
		}

		return new Endereco(
				resposta.cep().replaceAll("\\D", ""),
				resposta.logradouro(),
				resposta.bairro(),
				resposta.localidade(),
				resposta.uf());
	}

}
