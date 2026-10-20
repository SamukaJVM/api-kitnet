package com.kitnet.api.v1.mapper;

import org.mapstruct.Mapper;

import com.kitnet.api.v1.model.CepModel;
import com.kitnet.domain.model.Endereco;

@Mapper(componentModel = "spring")
public interface CepMapper {

	CepModel toModel(Endereco endereco);

}
