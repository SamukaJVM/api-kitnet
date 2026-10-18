package com.kitnet.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "predio")
@Getter
@Setter
public class Predio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Descrição é obrigatória")
	@Size(min = 3, max = 90, message = "Descrição deve ter entre 3 e 90 caracteres")
	private String descricao;

	@NotBlank(message = "CEP é obrigatório")
	@Pattern(regexp = "^\\d{8}$", message = "CEP deve conter exatamente 8 dígitos")
	private String cep;

	@Size(max = 255, message = "Logradouro não pode exceder 255 caracteres")
	private String logradouro;

	@Size(max = 255, message = "Complemento não pode exceder 255 caracteres")
	private String complemento;

	@Size(max = 90, message = "Bairro não pode exceder 90 caracteres")
	private String bairro;

	@Size(max = 90, message = "Localidade não pode exceder 90 caracteres")
	private String localidade;

	@Size(max = 2, message = "UF deve ter exatamente 2 caracteres")
	private String uf;

	@Size(max = 10, message = "Número não pode exceder 10 caracteres")
	private String numero;

	public void atualizarDados(Predio dados) {
		this.descricao = dados.descricao;
		this.cep = dados.cep;
		this.logradouro = dados.logradouro;
		this.complemento = dados.complemento;
		this.bairro = dados.bairro;
		this.localidade = dados.localidade;
		this.uf = dados.uf;
		this.numero = dados.numero;
	}

}
