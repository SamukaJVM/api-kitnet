package com.kitnet.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kitnet.domain.model.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, JpaSpecificationExecutor<Lancamento> {

	@Override
	@EntityGraph(attributePaths = {"apartamento", "inquilino"})
	Optional<Lancamento> findById(Long id);

	@Override
	@EntityGraph(attributePaths = {"apartamento", "inquilino"})
	Page<Lancamento> findAll(Specification<Lancamento> spec, Pageable pageable);

}
