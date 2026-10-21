package com.kitnet.domain.exception;

public class LancamentoNaoEncontradoException extends EntidadeNaoEncontradaException {

	public LancamentoNaoEncontradoException(String message) {
		super(message);
	}

	public LancamentoNaoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

}
