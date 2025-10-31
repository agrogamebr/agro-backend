package br.com.agrogame.agrogame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.repository.CompanyDocumentsRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;

@Service
public class ValidationService {
    
    @Autowired
    private CompanyDocumentsRepository companyDocumentRepository;
    
    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Valida se CNPJ já existe na base (busca em company_documents)
     */
    public boolean cnpjAlreadyExists(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return false;
        }
        return companyDocumentRepository.existsByDocumentNumberAndDocumentType_Code(cnpj, "CNPJ");
    }
    
    /**
     * Valida se email já existe na base
     */
    public boolean emailAlreadyExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return companyRepository.existsByEmail1(email);
    }
    
    /**
     * Valida formato de CNPJ (apenas números, 14 dígitos)
     */
    public boolean isValidCnpjFormat(String cnpj) {
        return cnpj != null && cnpj.matches("\\d{14}");
    }
    
    /**
     * Valida formato de email
     */
    public boolean isValidEmailFormat(String email) {
        return email != null && email.matches(".+@.+\\..+");
    }
}
