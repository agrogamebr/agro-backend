package br.com.agrogame.agrogame.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.CompanyDocumentDTO;
import br.com.agrogame.agrogame.dto.CreateCompanyDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CompanyDocument;
import br.com.agrogame.agrogame.model.CompanyDocumentType;
import br.com.agrogame.agrogame.model.CompanyStatus;
import br.com.agrogame.agrogame.model.CompanyType;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.CompanyDocumentTypeRepository;
import br.com.agrogame.agrogame.repository.CompanyDocumentsRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.CompanyStatusRepository;
import br.com.agrogame.agrogame.repository.CompanyTypeRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.repository.UserStatusRepository;
import br.com.agrogame.agrogame.repository.UserTypeRepository;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCredentialRepository authCredentialRepository;

    @Autowired
    private CompanyStatusRepository companyStatusRepository;

    @Autowired
    private CompanyTypeRepository companyTypeRepository;

    @Autowired
    private CompanyDocumentsRepository companyDocumentsRepository;

    @Autowired
    private CompanyDocumentTypeRepository companyDocumentTypeRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Company registerCompany(CreateCompanyDTO dto) {
        // 1. Criar Company (status PENDING) sem createdBy por enquanto
        Company company = fromDto(dto);
        Company savedCompany = companyRepository.save(company);

        // 2. Criar User (admin) vinculado à Company
        User admin = userFromDto(dto, savedCompany);
        User savedAdmin = userRepository.save(admin);

        // 3. Atualizar createdBy na Company para o admin
        savedCompany.setCreatedBy(savedAdmin);
        companyRepository.save(savedCompany);

        // 4. Criar AuthCredential para o admin
        AuthCredential cred = new AuthCredential();
        cred.setUser(savedAdmin);
        cred.setProvider("local");
        cred.setIdentifier(dto.getAdminEmail());
        cred.setPasswordHash(passwordEncoder.encode(dto.getAdminPassword()));
        cred.setIsActive(true);
        authCredentialRepository.save(cred);

        // 5. Salvar documentos vinculados
        for (CompanyDocumentDTO docDTO : dto.getDocumentos()) {
            CompanyDocument doc = new CompanyDocument();
            doc.setCompany(savedCompany);
            CompanyDocumentType docType = companyDocumentTypeRepository
                .findByCode(docDTO.getDocument().toString())
                .orElseThrow(() -> new RuntimeException(
                    "Tipo de documento não encontrado: " + docDTO.getDocument()));
            doc.setDocumentType(docType);
            doc.setDocumentNumber(docDTO.getDocumentNumber());
            doc.setIsPrimary(docDTO.isPrimary());
            companyDocumentsRepository.save(doc);
        }

        return savedCompany;
    }

    public Company fromDto(CreateCompanyDTO dto) {
        Company company = new Company();
        company.setFullCompanyName(dto.getFullCompanyName());
        company.setFantansyName(dto.getFantasyName());
        company.setEmail1(dto.getEmail1());
        company.setEmail2(dto.getEmail2());
        company.setPhone1(dto.getPhone1());
        company.setPhone2(dto.getPhone2());
        company.setAddress(dto.getAddress());
        company.setCity(dto.getCity());
        company.setState(dto.getState());
        company.setCountry(dto.getCountry());
        company.setResponsibleName(dto.getResponsibleName());
        company.setResponsiblePhone(dto.getResponsiblePhone());

        CompanyStatus status = companyStatusRepository
            .findByCode(EnumCompanyStatus.PENDING.toString())
            .orElseThrow(() -> new RuntimeException("Status 'PENDING' não encontrado"));
        company.setCompanyStatus(status);

        CompanyType companyType = companyTypeRepository
            .findById(dto.getCompanyTypeId())
            .orElseThrow(() -> new RuntimeException(
                "Tipo de empresa não encontrado: " + dto.getCompanyTypeId()));
        company.setCompanyType(companyType);

        return company;
    }

    private User userFromDto(CreateCompanyDTO dto, Company company) {
        User user = new User();
        user.setFullname(dto.getAdminName());
        // opcional: dividir nome em firstName/lastName
        String[] parts = dto.getAdminName().trim().split("\\s+", 2);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");

        user.setEmail1(dto.getAdminEmail());
        user.setEmail2(dto.getEmail2());  // se for o caso
        user.setCompany(company);

        UserType userType = userTypeRepository.findByCode("administrator")
            .orElseThrow(() -> new RuntimeException("UserType 'administrator' não encontrado"));
        user.setUserType(userType);

        UserStatus userStatus = userStatusRepository.findByCode("ACTIVE")
            .orElseThrow(() -> new RuntimeException("UserStatus 'ACTIVE' não encontrado"));
        user.setUserStatus(userStatus);

        return user;
}


    @Transactional
    public Company save(CreateCompanyDTO dto) {
        try {
            Company company = fromDto(dto);
            Company savedCompany = companyRepository.save(company);

            for (CompanyDocumentDTO docDTO : dto.getDocumentos()) {
                CompanyDocument doc = new CompanyDocument();
                doc.setCompany(savedCompany);

                CompanyDocumentType docType = companyDocumentTypeRepository.findByCode(docDTO.getDocument().toString())
                    .orElseThrow(() -> new RuntimeException(
                        "Tipo de documento não encontrado: " + docDTO.getDocument().toString()));

                doc.setDocumentType(docType);
                doc.setDocumentNumber(docDTO.getDocumentNumber());
                doc.setIsPrimary(docDTO.isPrimary());
                companyDocumentsRepository.save(doc);
            }

            return savedCompany;
        } catch (Exception e) {
            e.printStackTrace(); // Ou logger.error("Erro ao salvar empresa", e);
            throw new RuntimeException("Erro ao salvar empresa: " + e.getMessage(), e);
        }
    }

    public List<Company> findAll() {
        return companyRepository.findAllWithStatusAndType();
    }

}
