package com.kitnet.domain.exception;

public class CepNaoEncontradoException extends EntidadeNaoEncontradaException {

	public CepNaoEncontradoException(String message) {
		super(message);
	}

	public CepNaoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

}
