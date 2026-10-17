package com.kitnet.domain.exception;

public class EntidadeEmUsoException extends RuntimeException {

	public EntidadeEmUsoException(String message) {
		super(message);
	}

	public EntidadeEmUsoException(String message, Throwable cause) {
		super(message, cause);
	}
}
