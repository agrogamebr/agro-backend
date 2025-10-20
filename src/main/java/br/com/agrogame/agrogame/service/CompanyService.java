package br.com.agrogame.agrogame.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.CompanyDTO;
import br.com.agrogame.agrogame.dto.CompanyDocumentDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CompanyDocument;
import br.com.agrogame.agrogame.model.CompanyDocumentType;
import br.com.agrogame.agrogame.model.CompanyStatus;
import br.com.agrogame.agrogame.model.CompanyType;
import br.com.agrogame.agrogame.repository.CompanyDocumentTypeRepository;
import br.com.agrogame.agrogame.repository.CompanyDocumentsRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.CompanyStatusRepository;
import br.com.agrogame.agrogame.repository.CompanyTypeRepository;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository repository;

    @Autowired
    private CompanyDocumentsRepository companyDocumentRepository;
    
    @Autowired
    private CompanyStatusRepository companyStatusRepository;
    
    @Autowired
    private CompanyDocumentTypeRepository companyDocumentTypeRepository;
    
    @Autowired
    private CompanyTypeRepository companyTypeRepository;

//    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    public Company fromDto(CompanyDTO dto) {
        Company company = new Company();
        company.setFullCompanyName(dto.getFullCompanyName());
        company.setFantansyName(dto.getFantansyName());
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
        CompanyStatus status = companyStatusRepository.findByCode(EnumCompanyStatus.PENDING.toString()).orElseThrow(() -> new RuntimeException("Status 'PENDING' não encontrado"));
        company.setCompanyStatus(status);
        CompanyType companyType = companyTypeRepository.findById(dto.getCompanyTypeId()).orElseThrow(() -> new RuntimeException("Tipo de empresa não encontrado: " + dto.getCompanyTypeId()));
        company.setCompanyType(companyType);
//        company.setSenhaCriptografada(bcryptEncoder.encode(dto.getPassword()));

        return company;
    }

    @Transactional
    public Company save(CompanyDTO dto) {
        try {
            Company company = fromDto(dto);
            Company savedCompany = repository.save(company);

            for (CompanyDocumentDTO docDTO : dto.getDocumentos()) {
                CompanyDocument doc = new CompanyDocument();
                doc.setCompany(savedCompany);

                CompanyDocumentType docType = companyDocumentTypeRepository.findByCode(docDTO.getDocument().toString())
                    .orElseThrow(() -> new RuntimeException(
                        "Tipo de documento não encontrado: " + docDTO.getDocument().toString()));

                doc.setDocumentType(docType);
                doc.setDocumentNumber(docDTO.getDocumentNumber());
                doc.setIsPrimary(docDTO.isPrimary());
                companyDocumentRepository.save(doc);
            }

            return savedCompany;
        } catch (Exception e) {
            e.printStackTrace(); // Ou logger.error("Erro ao salvar empresa", e);
            throw new RuntimeException("Erro ao salvar empresa: " + e.getMessage(), e);
        }
    }

    public List<Company> findAll() {
        return repository.findAllWithStatusAndType();
    }

}
