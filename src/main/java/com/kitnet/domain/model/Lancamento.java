package com.kitnet.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import com.kitnet.domain.exception.NegocioException;

@Entity
@Table(name = "lancamento")
@Getter
@Setter
public class Lancamento {

	private static final int DIAS_MES_COMERCIAL = 30;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Apartamento é obrigatório")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "apartamento_id", nullable = false)
	private Apartamento apartamento;

	@NotNull(message = "Inquilino é obrigatório")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inquilino_id", nullable = false)
	private Inquilino inquilino;

	@NotNull
	@Column(name = "data_entrada", nullable = false)
	private LocalDate dataEntrada;

	@NotNull
	@Column(name = "data_pagamento", nullable = false)
	private LocalDate dataPagamento;

	@NotNull
	@Column(name = "valor_mensal", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorMensal;

	@Column(nullable = false)
	private int dias;

	@Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorTotal;

	@Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorPago = BigDecimal.ZERO;

	@Column(name = "valor_debito", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorDebito;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private StatusLancamento status = StatusLancamento.ABERTO;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_pagamento", length = 20)
	private StatusPagamento statusPagamento;

	@Size(max = 255, message = "Observação não pode exceder 255 caracteres")
	@Column(length = 255)
	@Nullable
	private String observacao;

	// Mês comercial de 30 dias: ciclo completo cobra o valorMensal exato; parcial é valorMensal × dias ÷ 30.
	public void calcularValores() {
		validarPeriodo();

		this.dias = (int) ChronoUnit.DAYS.between(dataEntrada, dataPagamento);
		boolean cicloCompleto = dataPagamento.isEqual(dataEntrada.plusMonths(1));
		this.valorTotal = cicloCompleto
			? valorMensal.setScale(2, RoundingMode.HALF_UP)
			: valorMensal.multiply(BigDecimal.valueOf(dias))
				.divide(BigDecimal.valueOf(DIAS_MES_COMERCIAL), 2, RoundingMode.HALF_UP);

		BigDecimal pago = valorPago == null ? BigDecimal.ZERO : valorPago;
		if (pago.compareTo(valorTotal) > 0) {
			throw new NegocioException("Valor pago não pode ser maior que o valor total do período");
		}
		this.valorPago = pago;
		this.valorDebito = valorTotal.subtract(pago);
		this.statusPagamento = valorDebito.compareTo(BigDecimal.ZERO) == 0 ? StatusPagamento.PAGO : StatusPagamento.DEBITO;
	}

	private void validarPeriodo() {
		if (!dataPagamento.isAfter(dataEntrada)) {
			throw new NegocioException("Data de pagamento deve ser posterior à data de entrada");
		}
		if (dataPagamento.isAfter(dataEntrada.plusMonths(1))) {
			throw new NegocioException("Período não pode ser maior que um ciclo mensal");
		}
	}

}
