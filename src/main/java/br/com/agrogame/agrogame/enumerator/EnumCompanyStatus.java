package br.com.agrogame.agrogame.enumerator;

public enum EnumCompanyStatus {
	APPROVED(1, "approved", "Aprovada"),
    PENDING(2, "pending", "Pendente de Aprovação"),
    REJECTED(3, "rejected", "Rejeitada"),
    SUSPENDED(4, "suspended", "Suspensa"),
    INACTIVE(5, "inactive", "Inativa"),
    ARCHIVED(6, "archived", "Arquivada");

	private final Integer id;
    private final String code;
    private final String description;

    EnumCompanyStatus(Integer id, String code, String description) {
    	this.id = id;
        this.code = code;
        this.description = description;
    }
    
	public Integer getId() {
		return id;
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

