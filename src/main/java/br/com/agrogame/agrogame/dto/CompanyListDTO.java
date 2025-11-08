package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class CompanyListDTO {
	private Integer companyId;
	private String fullCompanyName;
	private String fantansyName;
	private String email1;
	private String phone1;
	private String address;
	private String responsibleName;
	private String responsiblePhone;
	private String companyStatusDesc;
	private String companyTypeDesc;
	private LocalDateTime createdAt;

	public CompanyListDTO(br.com.agrogame.agrogame.model.Company company) {
		this.companyId = company.getId();
		this.fullCompanyName = company.getFullCompanyName();
		this.fantansyName = company.getFantansyName();
		this.email1 = company.getEmail1();
		this.phone1 = company.getPhone1();
		this.address = company.getAddress();
		this.responsibleName = company.getResponsibleName();
		this.responsiblePhone = company.getResponsiblePhone();
		this.companyStatusDesc = company.getCompanyStatus() != null ? company.getCompanyStatus().getName() : null;
		this.companyTypeDesc = company.getCompanyType() != null ? company.getCompanyType().getName() : null;
		this.createdAt = company.getCreatedAt();
	}

	public Integer getCompanyId() {
		return companyId;
	}


	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}


	public String getFullCompanyName() {
		return fullCompanyName;
	}

	public void setFullCompanyName(String fullCompanyName) {
		this.fullCompanyName = fullCompanyName;
	}

	public String getFantansyName() {
		return fantansyName;
	}

	public void setFantansyName(String fantansyName) {
		this.fantansyName = fantansyName;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getResponsibleName() {
		return responsibleName;
	}

	public void setResponsibleName(String responsibleName) {
		this.responsibleName = responsibleName;
	}

	public String getResponsiblePhone() {
		return responsiblePhone;
	}

	public void setResponsiblePhone(String responsiblePhone) {
		this.responsiblePhone = responsiblePhone;
	}

	public String getCompanyStatusDesc() {
		return companyStatusDesc;
	}

	public void setCompanyStatusDesc(String companyStatusDesc) {
		this.companyStatusDesc = companyStatusDesc;
	}

	public String getCompanyTypeDesc() {
		return companyTypeDesc;
	}

	public void setCompanyTypeDesc(String companyTypeDesc) {
		this.companyTypeDesc = companyTypeDesc;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}

