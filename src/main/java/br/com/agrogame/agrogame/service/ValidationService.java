package br.com.agrogame.agrogame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.repository.CompanyDocumentsRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class ValidationService {
    
    @Autowired
    private CompanyDocumentsRepository companyDocumentRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserDocumentRepository userDocumentRepository;

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
     * Valida se email já existe em Company OU User
     */
    public boolean emailAlreadyExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        boolean existsInCompany = companyRepository.existsByEmail1(email);
        boolean existsInUser = userRepository.existsByEmail1(email);

        return existsInCompany || existsInUser;
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
    
    public boolean documentAlreadyExists(Integer documentTypeId, String documentNumber) {
        // Remover formatação caso seja necessário (ex: CPF)
        String docNumberNormalized = documentNumber.replaceAll("[^A-Za-z0-9]", "");

        return userDocumentRepository.existsByDocumentTypeIdAndDocumentNumber(documentTypeId, docNumberNormalized);
    }

}
