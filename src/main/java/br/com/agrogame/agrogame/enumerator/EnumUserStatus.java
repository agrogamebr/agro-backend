package br.com.agrogame.agrogame.enumerator;

public enum EnumUserStatus {
    PENDING("pending", "Pendente de Aprovação"),
    APPROVED("approved", "Aprovado"),
    REJECTED("rejected", "Rejeitado"),
    INACTIVE("inactive", "Inativo"),
    ACTIVE("active", "Ativo");

    private final String code;
    private final String description;

    EnumUserStatus(String code, String description) {
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
    public static EnumUserStatus fromCode(String code) {
        for (EnumUserStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de usuário inválido: " + code);
    }
}
