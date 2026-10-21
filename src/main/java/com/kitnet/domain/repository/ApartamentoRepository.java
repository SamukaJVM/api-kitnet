package com.kitnet.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kitnet.domain.model.Apartamento;

public interface ApartamentoRepository extends JpaRepository<Apartamento, Long>, JpaSpecificationExecutor<Apartamento> {

	boolean existsByPredioIdAndNumero(Long predioId, String numero);

	boolean existsByPredioIdAndNumeroAndIdNot(Long predioId, String numero, Long id);

	boolean existsByMedidor(String medidor);

	boolean existsByMedidorAndIdNot(String medidor, Long id);

	@Override
	@EntityGraph(attributePaths = "predio")
	Optional<Apartamento> findById(Long id);

	@Override
	@EntityGraph(attributePaths = "predio")
	Page<Apartamento> findAll(Specification<Apartamento> spec, Pageable pageable);

}
