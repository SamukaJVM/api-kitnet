package com.kitnet.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;
import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inquilino")
@Getter
@Setter
public class Inquilino {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 90, message = "Nome não pode exceder 90 caracteres")
	@Column(length = 90, nullable = false)
	private String nome;

	@NotBlank(message = "Nome abreviado é obrigatório")
	@Size(max = 20, message = "Nome abreviado não pode exceder 20 caracteres")
	@Column(name = "nome_abreviado", length = 20, nullable = false)
	private String nomeAbreviado;

	@Email(message = "E-mail inválido")
	@Size(max = 90, message = "E-mail não pode exceder 90 caracteres")
	@Column(length = 90)
	@Nullable
	private String email;

	@Size(max = 15, message = "Contato não pode exceder 15 caracteres")
	@Column(length = 15)
	@Nullable
	private String contato;

	@CPF(message = "CPF inválido")
	@Column(length = 11)
	@Nullable
	private String cpf;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	@Nullable
	private Genero genero;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private StatusInquilino status = StatusInquilino.ATIVO;

	public void atualizarDados(Inquilino dados) {
		this.nome = dados.nome;
		this.nomeAbreviado = dados.nomeAbreviado;
		this.email = dados.email;
		this.contato = dados.contato;
		this.cpf = dados.cpf;
		this.genero = dados.genero;
		// status propositalmente não é alterado aqui: muda só pelos endpoints
		// explícitos de inativação/reativação.
	}

}
