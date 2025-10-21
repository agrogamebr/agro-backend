package br.com.agrogame.agrogame.enumerator;

public enum EnumCompanyDocumentType {

    CNPJ("cnpj", "CNPJ"),
    IE("ie", "Inscrição Estadual"),
    IM("im", "Inscrição Municipal"),
    ALVARA("alvara", "Alvará Sanitário"),
    CONTRATO_SOCIAL("contrato_social", "Contrato Social");

    private final String code;
    private final String name;

    EnumCompanyDocumentType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public static EnumCompanyDocumentType fromCode(String code) {
        for (EnumCompanyDocumentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        throw new IllegalArgumentException("Tipo de documento inválido: " + code);
    }
}
