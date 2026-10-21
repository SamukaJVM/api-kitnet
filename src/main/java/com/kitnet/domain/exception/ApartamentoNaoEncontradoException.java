package com.kitnet.domain.exception;

public class ApartamentoNaoEncontradoException extends EntidadeNaoEncontradaException {

	public ApartamentoNaoEncontradoException(String message) {
		super(message);
	}

	public ApartamentoNaoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

}
