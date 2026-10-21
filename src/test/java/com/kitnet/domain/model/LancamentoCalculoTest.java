package com.kitnet.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LancamentoCalculoTest {

	@ParameterizedTest(name = "[{index}] valorMensal={0} entrada={1} pagamento={2} -> dias={3} total={4}")
	@CsvSource({
		"270.00, 2026-04-01, 2026-05-01, 30, 270.00",
		"270.00, 2026-01-01, 2026-02-01, 31, 270.00",
		"270.00, 2026-02-01, 2026-03-01, 28, 270.00",
		"270.00, 2028-02-01, 2028-03-01, 29, 270.00",
		"270.00, 2026-01-31, 2026-02-28, 28, 270.00",
		"270.00, 2028-01-31, 2028-02-29, 29, 270.00",
		"270.00, 2026-12-15, 2027-01-15, 31, 270.00",
		"270.00, 2026-04-01, 2026-04-11, 10, 90.00",
		"270.00, 2026-01-20, 2026-02-10, 21, 189.00",
		"270.00, 2026-06-01, 2026-06-22, 21, 189.00",
		"270.00, 2026-01-01, 2026-01-31, 30, 270.00",
		"250.00, 2026-04-01, 2026-04-08, 7, 58.33",
		"270.15, 2026-04-01, 2026-04-02, 1, 9.01"
	})
	void deveCalcularDiasEValorTotal(
			BigDecimal valorMensal, LocalDate dataEntrada, LocalDate dataPagamento,
			int diasEsperados, String valorTotalEsperado) {
		Lancamento lancamento = new Lancamento();
		lancamento.setValorMensal(valorMensal);
		lancamento.setDataEntrada(dataEntrada);
		lancamento.setDataPagamento(dataPagamento);

		lancamento.calcularValores();

		assertThat(lancamento.getDias()).isEqualTo(diasEsperados);
		assertThat(lancamento.getValorTotal()).isEqualByComparingTo(valorTotalEsperado);
	}

	@Test
	void deveMarcarDebitoQuandoPagamentoParcial() {
		Lancamento lancamento = new Lancamento();
		lancamento.setValorMensal(new BigDecimal("270.00"));
		lancamento.setDataEntrada(LocalDate.of(2026, 4, 1));
		lancamento.setDataPagamento(LocalDate.of(2026, 4, 11));
		lancamento.setValorPago(new BigDecimal("50.00"));

		lancamento.calcularValores();

		assertThat(lancamento.getValorDebito()).isEqualByComparingTo("40.00");
		assertThat(lancamento.getStatusPagamento()).isEqualTo(StatusPagamento.DEBITO);
	}

	@Test
	void deveMarcarPagoQuandoPagamentoTotal() {
		Lancamento lancamento = new Lancamento();
		lancamento.setValorMensal(new BigDecimal("270.00"));
		lancamento.setDataEntrada(LocalDate.of(2026, 4, 1));
		lancamento.setDataPagamento(LocalDate.of(2026, 4, 11));
		lancamento.setValorPago(new BigDecimal("90.00"));

		lancamento.calcularValores();

		assertThat(lancamento.getValorDebito()).isEqualByComparingTo("0.00");
		assertThat(lancamento.getStatusPagamento()).isEqualTo(StatusPagamento.PAGO);
	}

}
