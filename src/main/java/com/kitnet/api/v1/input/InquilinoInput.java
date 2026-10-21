package com.kitnet.api.v1.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;
import org.jspecify.annotations.Nullable;

import com.kitnet.domain.model.Genero;

public record InquilinoInput(
	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 90, message = "Nome não pode exceder 90 caracteres")
	String nome,

	@NotBlank(message = "Nome abreviado é obrigatório")
	@Size(max = 20, message = "Nome abreviado não pode exceder 20 caracteres")
	String nomeAbreviado,

	@Email(message = "E-mail inválido")
	@Size(max = 90, message = "E-mail não pode exceder 90 caracteres")
	@Nullable
	String email,

	@Size(max = 15, message = "Contato não pode exceder 15 caracteres")
	@Nullable
	String contato,

	@CPF(message = "CPF inválido")
	@Nullable
	String cpf,

	@Nullable
	Genero genero
) {

	public InquilinoInput {
		email = normalizarEmail(email);
		contato = normalizarBranco(contato);
		cpf = normalizarCpf(cpf);
	}

	private static @Nullable String normalizarEmail(@Nullable String email) {
		if (email == null) {
			return null;
		}
		String normalizado = email.trim().toLowerCase();
		return normalizado.isBlank() ? null : normalizado;
	}

	private static @Nullable String normalizarBranco(@Nullable String valor) {
		if (valor == null) {
			return null;
		}
		String normalizado = valor.trim();
		return normalizado.isBlank() ? null : normalizado;
	}

	private static @Nullable String normalizarCpf(@Nullable String cpf) {
		if (cpf == null) {
			return null;
		}
		String somenteDigitos = cpf.replaceAll("\\D", "");
		return somenteDigitos.isBlank() ? null : somenteDigitos;
	}
}
