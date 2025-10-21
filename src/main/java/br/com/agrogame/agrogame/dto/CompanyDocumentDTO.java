package br.com.agrogame.agrogame.dto;

import br.com.agrogame.agrogame.enumerator.EnumCompanyDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyDocumentDTO {

    @NotNull
    private EnumCompanyDocumentType type;

    private boolean isPrimary;
    
    @NotBlank
    private String documentNumber;
    

	public EnumCompanyDocumentType getType() {
		return type;
	}

	public void setType(EnumCompanyDocumentType type) {
		this.type = type;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public EnumCompanyDocumentType getDocument() {
		return type;
	}

	public void setDocument(EnumCompanyDocumentType document) {
		this.type = document;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
    
}
