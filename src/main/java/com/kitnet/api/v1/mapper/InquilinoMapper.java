package com.kitnet.api.v1.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.kitnet.api.v1.input.InquilinoInput;
import com.kitnet.api.v1.model.InquilinoModel;
import com.kitnet.domain.model.Inquilino;

@Mapper(componentModel = "spring")
public interface InquilinoMapper {

	Inquilino toDomain(InquilinoInput inquilinoInput);

	InquilinoModel toModel(Inquilino inquilino);

	List<InquilinoModel> toModelList(List<Inquilino> inquilinos);

}
