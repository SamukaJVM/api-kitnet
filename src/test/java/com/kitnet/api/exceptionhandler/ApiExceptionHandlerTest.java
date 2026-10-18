package com.kitnet.api.exceptionhandler;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.WebRequest;

import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.EntidadeNaoEncontradaException;
import com.kitnet.domain.exception.NegocioException;

class ApiExceptionHandlerTest {

	private final ApiExceptionHandler handler = new ApiExceptionHandler();
	private final Instant beforeTest = Instant.now();
	private final WebRequest webRequest = null;

	@Test
	void shouldHandleNegocioException() {
		NegocioException ex = new NegocioException("Regra de negócio violada");
		var response = handler.handleNegocioException(ex, webRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		ProblemDetail problem = response.getBody();
		assertThat(problem).isNotNull();
		assertThat(problem.getStatus()).isEqualTo(400);
		assertThat(problem.getType()).isEqualTo(URI.create("https://kitnet.com/problemas/negocio"));
		assertThat(problem.getTitle()).isEqualTo("Violação de regra de negócio");
		assertThat(problem.getDetail()).isEqualTo("Regra de negócio violada");
		assertThat(problem.getProperties()).containsKeys("userMessage", "timestamp");
		assertThat(problem.getProperties().get("userMessage")).isEqualTo("Regra de negócio violada");
		assertThat((Instant) problem.getProperties().get("timestamp")).isAfterOrEqualTo(beforeTest);
	}

	@Test
	void shouldHandleEntidadeNaoEncontradaException() {
		EntidadeNaoEncontradaException ex = new TestEntidadeNaoEncontradaException("Entidade não encontrada");
		var response = handler.handleEntidadeNaoEncontrada(ex, webRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		ProblemDetail problem = response.getBody();
		assertThat(problem).isNotNull();
		assertThat(problem.getStatus()).isEqualTo(404);
		assertThat(problem.getType()).isEqualTo(URI.create("https://kitnet.com/problemas/entidade-nao-encontrada"));
		assertThat(problem.getTitle()).isEqualTo("Entidade não encontrada");
		assertThat(problem.getDetail()).isEqualTo("Entidade não encontrada");
		assertThat(problem.getProperties()).containsKeys("userMessage", "timestamp");
		assertThat(problem.getProperties().get("userMessage")).isEqualTo("Entidade não encontrada");
		assertThat((Instant) problem.getProperties().get("timestamp")).isAfterOrEqualTo(beforeTest);
	}

	@Test
	void shouldHandleEntidadeEmUsoException() {
		EntidadeEmUsoException ex = new EntidadeEmUsoException("Entidade em uso");
		var response = handler.handleEntidadeEmUso(ex, webRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		ProblemDetail problem = response.getBody();
		assertThat(problem).isNotNull();
		assertThat(problem.getStatus()).isEqualTo(409);
		assertThat(problem.getType()).isEqualTo(URI.create("https://kitnet.com/problemas/entidade-em-uso"));
		assertThat(problem.getTitle()).isEqualTo("Entidade em uso");
		assertThat(problem.getDetail()).isEqualTo("Entidade em uso");
		assertThat(problem.getProperties()).containsKeys("userMessage", "timestamp");
		assertThat(problem.getProperties().get("userMessage")).isEqualTo("Entidade em uso");
		assertThat((Instant) problem.getProperties().get("timestamp")).isAfterOrEqualTo(beforeTest);
	}

	@Test
	void shouldHandleGenericException() {
		Exception ex = new RuntimeException("Erro inesperado");
		var response = handler.handleGenericException(ex, webRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		ProblemDetail problem = response.getBody();
		assertThat(problem).isNotNull();
		assertThat(problem.getStatus()).isEqualTo(500);
		assertThat(problem.getType()).isEqualTo(URI.create("https://kitnet.com/problemas/erro-interno"));
		assertThat(problem.getTitle()).isEqualTo("Erro interno do servidor");
		assertThat(problem.getDetail()).isEqualTo("Erro interno do servidor");
		assertThat(problem.getProperties()).containsKeys("userMessage", "timestamp");
		assertThat(problem.getProperties().get("userMessage")).isEqualTo("Ocorreu um erro inesperado. Por favor, tente novamente.");
		assertThat(problem.getDetail()).doesNotContain("stacktrace", "Exception");
		assertThat((Instant) problem.getProperties().get("timestamp")).isAfterOrEqualTo(beforeTest);
	}

	private static class TestEntidadeNaoEncontradaException extends EntidadeNaoEncontradaException {
		TestEntidadeNaoEncontradaException(String message) {
			super(message);
		}
	}
}
