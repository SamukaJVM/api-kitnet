package com.kitnet.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kitnet.domain.model.Predio;

public interface PredioRepository extends JpaRepository<Predio, Long> {

	boolean existsByDescricao(String descricao);

	boolean existsByDescricaoAndIdNot(String descricao, Long id);

}
