package com.kitnet.api.exceptionhandler;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kitnet.domain.exception.EntidadeEmUsoException;
import com.kitnet.domain.exception.EntidadeNaoEncontradaException;
import com.kitnet.domain.exception.NegocioException;
import com.kitnet.domain.exception.ServicoExternoIndisponivelException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<ProblemDetail> handleNegocioException(NegocioException ex, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			ex.getMessage()
		);
		problem.setType(URI.create("https://kitnet.com/problemas/negocio"));
		problem.setTitle("Violação de regra de negócio");
		problem.setProperty("userMessage", ex.getMessage());
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(EntidadeNaoEncontradaException.class)
	public ResponseEntity<ProblemDetail> handleEntidadeNaoEncontrada(EntidadeNaoEncontradaException ex, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.NOT_FOUND,
			ex.getMessage()
		);
		problem.setType(URI.create("https://kitnet.com/problemas/entidade-nao-encontrada"));
		problem.setTitle("Entidade não encontrada");
		problem.setProperty("userMessage", ex.getMessage());
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<ProblemDetail> handleEntidadeEmUso(EntidadeEmUsoException ex, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.CONFLICT,
			ex.getMessage()
		);
		problem.setType(URI.create("https://kitnet.com/problemas/entidade-em-uso"));
		problem.setTitle("Entidade em uso");
		problem.setProperty("userMessage", ex.getMessage());
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	@ExceptionHandler(ServicoExternoIndisponivelException.class)
	public ResponseEntity<ProblemDetail> handleServicoExternoIndisponivel(ServicoExternoIndisponivelException ex, WebRequest request) {
		log.error("Serviço externo indisponível", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.SERVICE_UNAVAILABLE,
			"Serviço externo temporariamente indisponível. Tente novamente mais tarde."
		);
		problem.setType(URI.create("https://kitnet.com/problemas/servico-externo-indisponivel"));
		problem.setTitle("Serviço externo indisponível");
		problem.setProperty("userMessage", "Serviço externo temporariamente indisponível. Tente novamente mais tarde.");
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
		log.error("Erro não tratado", ex);
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Erro interno do servidor"
		);
		problem.setType(URI.create("https://kitnet.com/problemas/erro-interno"));
		problem.setTitle("Erro interno do servidor");
		problem.setProperty("userMessage", "Ocorreu um erro inesperado. Por favor, tente novamente.");
		problem.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex,
		HttpHeaders headers,
		HttpStatusCode status,
		WebRequest request
	) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			"Validação falhou"
		);
		problem.setType(URI.create("https://kitnet.com/problemas/validacao"));
		problem.setTitle("Erro de validação");
		problem.setProperty("userMessage", "Os dados fornecidos são inválidos");
		problem.setProperty("timestamp", Instant.now());

		Map<String, String> fields = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
			fields.put(error.getField(), error.getDefaultMessage())
		);
		problem.setProperty("fields", fields);

		return ResponseEntity.badRequest().body(problem);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
		HandlerMethodValidationException ex,
		HttpHeaders headers,
		HttpStatusCode status,
		WebRequest request
	) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST,
			"Validação falhou"
		);
		problem.setType(URI.create("https://kitnet.com/problemas/validacao"));
		problem.setTitle("Erro de validação");
		problem.setProperty("userMessage", "Os dados fornecidos são inválidos");
		problem.setProperty("timestamp", Instant.now());

		Map<String, String> fields = new LinkedHashMap<>();
		ex.getParameterValidationResults().forEach(result ->
			result.getResolvableErrors().forEach(error ->
				fields.put(result.getMethodParameter().getParameterName(), error.getDefaultMessage())
			)
		);
		problem.setProperty("fields", fields);

		return ResponseEntity.badRequest().body(problem);
	}
}
