package br.com.agrogame.agrogame.dto;

public class BackofficeEmployeeSummaryDTO {
    private Integer userId;
    private String fullName;
    private String email;
    private Integer userTypeId;
    private String userTypeName;
    private String telefone;

    private String documentNumber;
    private Integer documentTypeId;
    private String documentTypeName;

    private Integer farmId;
    private String farmName;
    
	public BackofficeEmployeeSummaryDTO(Integer userId, String fullName, String email, Integer userTypeId,
			String userTypeName, String documentNumber, Integer documentTypeId, String documentTypeName, Integer farmId,
			String farmName, String telefone) {
		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.userTypeId = userTypeId;
		this.userTypeName = userTypeName;
		this.documentNumber = documentNumber;
		this.documentTypeId = documentTypeId;
		this.documentTypeName = documentTypeName;
		this.farmId = farmId;
		this.farmName = farmName;
		this.telefone = telefone;
	}
	

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public Integer getDocumentTypeId() {
		return documentTypeId;
	}

	public void setDocumentTypeId(Integer documentTypeId) {
		this.documentTypeId = documentTypeId;
	}

	public String getDocumentTypeName() {
		return documentTypeName;
	}

	public void setDocumentTypeName(String documentTypeName) {
		this.documentTypeName = documentTypeName;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}

	public Integer getUserTypeId() {
		return userTypeId;
	}

	public void setUserTypeId(Integer userTypeId) {
		this.userTypeId = userTypeId;
	}

	public String getUserTypeName() {
		return userTypeName;
	}

	public void setUserTypeName(String userTypeName) {
		this.userTypeName = userTypeName;
	}
	
	public String getTelefone() {
		return telefone;
	}
	
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
}
