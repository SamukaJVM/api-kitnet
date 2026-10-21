package com.kitnet.api.v1.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.kitnet.api.v1.input.ApartamentoInput;
import com.kitnet.api.v1.model.ApartamentoModel;
import com.kitnet.api.v1.model.PredioResumoModel;
import com.kitnet.domain.model.Apartamento;
import com.kitnet.domain.model.Predio;

@Mapper(componentModel = "spring")
public interface ApartamentoMapper {

	@Mapping(target = "predio", ignore = true)
	Apartamento toDomain(ApartamentoInput apartamentoInput);

	ApartamentoModel toModel(Apartamento apartamento);

	List<ApartamentoModel> toModelList(List<Apartamento> apartamentos);

	PredioResumoModel toPredioResumo(Predio predio);

}
