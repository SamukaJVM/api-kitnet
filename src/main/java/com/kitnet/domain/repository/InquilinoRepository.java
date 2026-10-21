package com.kitnet.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kitnet.domain.model.Inquilino;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long>, JpaSpecificationExecutor<Inquilino> {

	boolean existsByCpf(String cpf);

	boolean existsByCpfAndIdNot(String cpf, Long id);

}
