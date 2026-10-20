package com.kitnet.domain.service;

import com.kitnet.domain.model.Endereco;

public interface EnderecoCepFinder {

	Endereco buscarPorCep(String cep);

}
