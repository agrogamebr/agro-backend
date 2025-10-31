package br.com.agrogame.agrogame.enumerator;

public enum EnumCompanyStatus {
    PENDING("pending", "Pendente de Aprovação"),
    APPROVED("approved", "Aprovada"),
    REJECTED("rejected", "Rejeitada"),
    SUSPENDED("suspended", "Suspensa"),
    INACTIVE("inactive", "Inativa"),
    ARCHIVED("archived", "Arquivada");

    private final String code;
    private final String description;

    EnumCompanyStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Encontra o enum pelo code (minúsculo)
     */
    public static EnumCompanyStatus fromCode(String code) {
        for (EnumCompanyStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status inválido: " + code);
    }
}

