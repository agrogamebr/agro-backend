package br.com.agrogame.agrogame.dto;

import java.util.List;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyDTO {

    // ===== DADOS DA EMPRESA =====
    
    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(max = 100, message = "Nome não pode exceder 100 caracteres")
    private String fullCompanyName;

    @NotBlank(message = "Nome fantasia é obrigatório")
    private String fantasyName;

    @NotBlank(message = "Email corporativo é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email1;

    @Email(message = "Email deve ser válido")
    private String email2;

    @NotBlank(message = "Telefone/WhatsApp é obrigatório")
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String phone1;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String phone2;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ser a sigla (ex: SP)")
    private String state;

    @NotBlank(message = "País é obrigatório")
    private String country;

    // ===== RESPONSÁVEL PELA EMPRESA =====
    
    @NotBlank(message = "Nome do responsável é obrigatório")
    private String responsibleName;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String responsiblePhone;

    // ===== DOCUMENTOS (CNPJ virá aqui) =====
    
    @NotNull(message = "Documentos são obrigatórios")
    @NotEmpty(message = "Pelo menos um documento é obrigatório")
    private List<CompanyDocumentDTO> documentos;

    // ===== SEGMENTO E TIPO =====
    
    @NotBlank(message = "Segmento é obrigatório")
    private String segment;

    @NotNull(message = "Tipo de empresa é obrigatório")
    private Integer companyTypeId;

    @NotNull(message = "Segmento é obrigatório")
    private Integer segmentoId;

    // ===== DADOS DO USUÁRIO ADMINISTRADOR PADRÃO =====
    
    // REMOVIDO: adminName e adminEmail NÃO são mais necessários
    // O sistema usará responsibleName para o usuário
    // e email1 (email corporativo) como credencial de acesso
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Senha deve conter letras maiúsculas, minúsculas, números e caracteres especiais (@$!%*?&)"
    )
    private String adminPassword;

    // ===== ACEITE DE TERMOS =====
    
    @AssertTrue(message = "Você deve aceitar os termos de uso")
    private boolean aceiteTermos;

    // ===== GETTERS E SETTERS =====

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

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
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

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public boolean isAceiteTermos() {
        return aceiteTermos;
    }

    public void setAceiteTermos(boolean aceiteTermos) {
        this.aceiteTermos = aceiteTermos;
    }
}
