package com.kitnet.api.v1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kitnet.api.v1.mapper.CepMapper;
import com.kitnet.api.v1.model.CepModel;
import com.kitnet.api.v1.openapi.CepControllerOpenApi;
import com.kitnet.domain.model.Endereco;
import com.kitnet.domain.service.EnderecoCepFinder;

@RestController
@RequestMapping("/v1/ceps")
public class CepController implements CepControllerOpenApi {

	private final EnderecoCepFinder enderecoCepFinder;
	private final CepMapper cepMapper;

	public CepController(EnderecoCepFinder enderecoCepFinder, CepMapper cepMapper) {
		this.enderecoCepFinder = enderecoCepFinder;
		this.cepMapper = cepMapper;
	}

	@GetMapping("/{cep}")
	@Override
	public CepModel buscar(@PathVariable String cep) {
		Endereco endereco = enderecoCepFinder.buscarPorCep(cep);
		return cepMapper.toModel(endereco);
	}

}
