package br.com.agrogame.auth.util;

import java.util.regex.Pattern;

public class IdentifierValidator {

	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	private static final String CPF_REGEX = "^\\d{11}$";
	private static final String CNPJ_REGEX = "^\\d{14}$";

	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
	private static final Pattern CPF_PATTERN = Pattern.compile(CPF_REGEX);
	private static final Pattern CNPJ_PATTERN = Pattern.compile(CNPJ_REGEX);

	public enum IdentifierType {
		EMAIL("EMAIL"), CPF("CPF"), CNPJ("CNPJ"), INVALID("INVALID");

		private final String value;

		IdentifierType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static String normalize(String identifier) {
		if (identifier == null || identifier.trim().isEmpty()) {
			return null;
		}
		return identifier.replaceAll("[\\s.\\-/()@]", "");
	}

	public static IdentifierType detectType(String identifier) {
		String normalized = normalize(identifier);

		if (normalized == null || normalized.isEmpty()) {
			return IdentifierType.INVALID;
		}

		if (identifier.contains("@")) {
			return EMAIL_PATTERN.matcher(identifier).matches() ? IdentifierType.EMAIL : IdentifierType.INVALID;
		}

		if (CPF_PATTERN.matcher(normalized).matches()) {
			return IdentifierType.CPF;
		} else if (CNPJ_PATTERN.matcher(normalized).matches()) {
			return IdentifierType.CNPJ;
		}

		return IdentifierType.INVALID;
	}

	public static boolean isValid(String identifier) {
		return detectType(identifier) != IdentifierType.INVALID;
	}

	public static String getNormalizedIfValid(String identifier) {
		if (!isValid(identifier)) {
			throw new IllegalArgumentException(
					"Identificador inválido. Aceitar email, CPF (11 dígitos) ou CNPJ (14 dígitos).");
		}

		if (detectType(identifier) == IdentifierType.EMAIL) {
			return identifier.toLowerCase();
		}

		return normalize(identifier);
	}

	/**
	 * Normaliza CEP removendo caracteres especiais, mantendo apenas números.
	 * Exemplo: "38400-172" → "38400172"
	 */
	public static String normalizeZipcode(String zipcode) {
		if (zipcode == null || zipcode.trim().isEmpty()) {
			return null;
		}
		return zipcode.replaceAll("[^0-9]", "");
	}
}
