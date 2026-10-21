package com.kitnet.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "apartamento")
@Getter
@Setter
public class Apartamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Número é obrigatório")
	@Size(min = 1, max = 10, message = "Número deve ter entre 1 e 10 caracteres")
	@Column(length = 10, nullable = false)
	private String numero;

	@NotBlank(message = "Descrição é obrigatória")
	@Size(min = 1, max = 90, message = "Descrição deve ter entre 1 e 90 caracteres")
	@Column(length = 90, nullable = false)
	private String descricao;

	@Size(max = 10, message = "Medidor não pode exceder 10 caracteres")
	@Column(length = 10)
	private String medidor;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private StatusApartamento status = StatusApartamento.DISPONIVEL;

	@NotNull(message = "Prédio é obrigatório")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "predio_id", nullable = false)
	private Predio predio;

	@NotNull(message = "Valor do aluguel é obrigatório")
	@DecimalMin(value = "0.01", message = "Valor do aluguel deve ser maior que zero")
	@Digits(integer = 8, fraction = 2, message = "Valor do aluguel deve ter no máximo 8 dígitos inteiros e 2 decimais")
	@Column(name = "valor_aluguel", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorAluguel;

	public void atualizarDados(Apartamento dados) {
		this.numero = dados.numero;
		this.descricao = dados.descricao;
		this.medidor = dados.medidor;
		this.predio = dados.predio;
		this.valorAluguel = dados.valorAluguel;
		// status propositalmente não é alterado aqui: muda só por regra de lançamento
		// ou pelos endpoints explícitos de manutenção (fora deste CRUD).
	}

	public void ocupar() {
		this.status = StatusApartamento.OCUPADO;
	}

	public void liberar() {
		this.status = StatusApartamento.DISPONIVEL;
	}

}
