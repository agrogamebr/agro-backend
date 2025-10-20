package br.com.agrogame.agrogame.dto;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompanyDTO {

    @NotBlank
    @Size(max = 100)
    private String fullCompanyName;

    @NotBlank
    private String fantansyName;

    @NotBlank
    @Email(message = "Email corporativo inválido")
    private String email1;

    private String email2;

    @NotBlank
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone inválido. Exemplo: (11) 98765-4321")
    private String phone1;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone inválido. Exemplo: (11) 98765-4321")
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

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}:;<>,.?/~`\\-]).{8,}$",
        message = "Senha deve ter ao menos 8 caracteres, uma letra maiúscula, um número e um caractere especial"
    )
    private String password;

    @NotNull(message = "É obrigatório selecionar o segmento de atuação")
    private Integer segmentoId;

    @AssertTrue(message = "Você deve aceitar os Termos de Uso")
    private boolean aceiteTermos;

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

	
	public Integer getCompanyTypeId() {
		return companyTypeId;
	}

	public void setCompanyTypeId(Integer companyTypeId) {
		this.companyTypeId = companyTypeId;
	}

	public List<CompanyDocumentDTO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<CompanyDocumentDTO> documentos) {
		this.documentos = documentos;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

}
