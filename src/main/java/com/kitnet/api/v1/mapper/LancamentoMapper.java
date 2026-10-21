package com.kitnet.api.v1.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kitnet.api.v1.input.LancamentoInput;
import com.kitnet.api.v1.model.ApartamentoResumoModel;
import com.kitnet.api.v1.model.InquilinoResumoModel;
import com.kitnet.api.v1.model.LancamentoModel;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.model.Inquilino;
import com.kitnet.domain.model.Lancamento;

@Mapper(componentModel = "spring")
public interface LancamentoMapper {

	@Mapping(target = "apartamento", ignore = true)
	@Mapping(target = "inquilino", ignore = true)
	Lancamento toDomain(LancamentoInput lancamentoInput);

	LancamentoModel toModel(Lancamento lancamento);

	List<LancamentoModel> toModelList(List<Lancamento> lancamentos);

	ApartamentoResumoModel toApartamentoResumo(Apartamento apartamento);

	InquilinoResumoModel toInquilinoResumo(Inquilino inquilino);

}
