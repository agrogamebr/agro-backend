package br.com.agrogame.agrogame.dto;

public class BackofficeEmployeeSummaryDTO {
	private Integer userId;
	private String fullName;
	private String email1;
	private String email2;
	private Integer userTypeId;
	private String userTypeName;
	private String telefone;

	private String documentNumber;
	private Integer documentTypeId;
	private String documentTypeCode;

	private String address;
	private String addressNumber;
	private String zipCode;
	private String city;
	private String state;

	public BackofficeEmployeeSummaryDTO(Integer userId, String fullName, String email1, String email2,
			Integer userTypeId, String userTypeName, String documentNumber, Integer documentTypeId,
			String documentTypeCode, String telefone, String address,
			String addressNumber, String zipCode, String city, String state) {
		this.userId = userId;
		this.fullName = fullName;
		this.email1 = email1;
		this.email2 = email2;
		this.userTypeId = userTypeId;
		this.userTypeName = userTypeName;
		this.documentNumber = documentNumber;
		this.documentTypeId = documentTypeId;
		this.documentTypeCode = documentTypeCode;
		this.telefone = telefone;
		this.address = address;
		this.addressNumber = addressNumber;
		this.zipCode = zipCode;
		this.city = city;
		this.state = state;
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

	public String getDocumentTypeCode() {
		return documentTypeCode;
	}

	public void setDocumentTypeCode(String documentTypeCode) {
		this.documentTypeCode = documentTypeCode;
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

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressNumber() {
		return addressNumber;
	}

	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
