package br.com.agrogame.agrogame.enumerator;

public enum EnumUserType {
    ADMINISTRATOR("administrator", "Administrador"),
    MANAGER("manager", "Gerente"),
    EMPLOYEE("employee", "Funcionário"),
    USER("user", "Usuário"),
    PARTNER("partner", "Parceiro"),
    AUDITOR("auditor", "Auditor"),
    GUEST("guest", "Convidado");

    private final String code;
    private final String description;

    EnumUserType(String code, String description) {
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
    public static EnumUserType fromCode(String code) {
        for (EnumUserType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de usuário inválido: " + code);
    }
}
