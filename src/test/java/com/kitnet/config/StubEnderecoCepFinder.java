package com.kitnet.config;

import java.util.function.Function;

import com.kitnet.domain.model.Endereco;
import com.kitnet.domain.service.EnderecoCepFinder;

public class StubEnderecoCepFinder implements EnderecoCepFinder {

	private Function<String, Endereco> comportamento = StubEnderecoCepFinder::comportamentoPadrao;

	@Override
	public Endereco buscarPorCep(String cep) {
		return comportamento.apply(cep);
	}

	public void retornar(Endereco endereco) {
		this.comportamento = cep -> endereco;
	}

	public void lancar(RuntimeException exception) {
		this.comportamento = cep -> {
			throw exception;
		};
	}

	public void reset() {
		this.comportamento = StubEnderecoCepFinder::comportamentoPadrao;
	}

	private static Endereco comportamentoPadrao(String cep) {
		throw new IllegalStateException(
			"StubEnderecoCepFinder não foi configurado para este teste. Chame retornar(...) ou lancar(...).");
	}

}
