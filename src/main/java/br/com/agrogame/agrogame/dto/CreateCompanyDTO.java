package br.com.agrogame.agrogame.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCompanyDTO {

    // Dados da empresa
    @NotBlank @Size(max = 100)
    private String fullCompanyName;

    @NotBlank
    private String fantasyName;

    @NotBlank @Email
    private String email1;

    private String email2;

    @NotBlank @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}")
    private String phone1;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}")
    private String phone2;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    private String responsibleName;
    private String responsiblePhone;

    @NotNull
    private List<CompanyDocumentDTO> documentos;

    @NotNull
    private Integer companyTypeId;

    @NotNull
    private Integer segmentoId;

    @AssertTrue
    private boolean aceiteTermos;

    // Dados do usuário administrador
    @NotBlank
    private String adminName;

    @NotBlank @Email
    private String adminEmail;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;

	public String getFullCompanyName() {
		return fullCompanyName;
	}

	public void setFullCompanyName(String fullCompanyName) {
		this.fullCompanyName = fullCompanyName;
	}

	public String getFantasyName() {
		return fantasyName;
	}

	public void setFantasyName(String fantasyName) {
		this.fantasyName = fantasyName;
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

	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
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

	public List<CompanyDocumentDTO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<CompanyDocumentDTO> documentos) {
		this.documentos = documentos;
	}

	public Integer getCompanyTypeId() {
		return companyTypeId;
	}

	public void setCompanyTypeId(Integer companyTypeId) {
		this.companyTypeId = companyTypeId;
	}

	public Integer getSegmentoId() {
		return segmentoId;
	}

	public void setSegmentoId(Integer segmentoId) {
		this.segmentoId = segmentoId;
	}

	public boolean isAceiteTermos() {
		return aceiteTermos;
	}

	public void setAceiteTermos(boolean aceiteTermos) {
		this.aceiteTermos = aceiteTermos;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
    
    
}
