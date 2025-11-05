package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuralProducerDTO {
    
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String fullName;
    
    @NotNull(message = "Tipo de documento é obrigatório")
    private Integer documentTypeId; // 1=CPF, 2=RG, 3=CNH, 4=Passport
    
    @NotBlank(message = "Número do documento é obrigatório")
    @Size(max = 50, message = "Número do documento deve ter no máximo 50 caracteres")
    private String documentNumber;
    
    @Pattern(
        regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}",
        message = "Telefone deve estar no formato (XX) XXXXX-XXXX"
    )
    private String phone;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    private String email;
    
    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (sigla)")
    private String state;
    
    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String address; 
    
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String number; 
    
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String city; 
    
    @Pattern(
        regexp = "\\d{5}-?\\d{3}",
        message = "CEP deve estar no formato XXXXX-XXX ou XXXXXXXX"
    )
    private String zipcode; // CEP
    
    @NotNull(message = "Empresa parceira é obrigatória")
    private Integer companyId;
    
    @NotBlank(message = "Senha é obrigatória")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
        message = "Senha deve ter no mínimo 8 caracteres, 1 letra maiúscula, 1 número e 1 caractere especial"
    )
    private String password;
    
    @NotNull(message = "Você deve aceitar os termos de uso")
    @AssertTrue(message = "Você deve aceitar os termos de uso")
    private Boolean acceptedTerms;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Integer documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAcceptedTerms() {
        return acceptedTerms;
    }

    public void setAcceptedTerms(Boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
