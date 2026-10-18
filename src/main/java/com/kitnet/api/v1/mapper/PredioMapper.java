package com.kitnet.api.v1.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.api.v1.model.PredioModel;
import com.kitnet.domain.model.Predio;

@Mapper(componentModel = "spring")
public interface PredioMapper {

	Predio toDomain(PredioInput predioInput);

	PredioModel toModel(Predio predio);

	List<PredioModel> toModelList(List<Predio> predios);

}
