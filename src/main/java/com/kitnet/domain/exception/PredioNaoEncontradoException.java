package com.kitnet.domain.exception;

public class PredioNaoEncontradoException extends EntidadeNaoEncontradaException {

	public PredioNaoEncontradoException(String message) {
		super(message);
	}

	public PredioNaoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

}
