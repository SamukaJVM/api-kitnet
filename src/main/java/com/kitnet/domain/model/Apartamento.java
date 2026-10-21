package com.kitnet.domain.model;

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

	public void atualizarDados(Apartamento dados) {
		this.numero = dados.numero;
		this.descricao = dados.descricao;
		this.medidor = dados.medidor;
		this.predio = dados.predio;
		// status propositalmente não é alterado aqui: muda só por regra de lançamento
		// ou pelos endpoints explícitos de manutenção (fora deste CRUD).
	}

}
