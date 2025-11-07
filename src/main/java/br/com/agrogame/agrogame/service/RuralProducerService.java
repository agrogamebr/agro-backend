package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocument;
import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.RuralProducerRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserDocumentTypeRepository;
import br.com.agrogame.agrogame.repository.UserStatusRepository;
import br.com.agrogame.agrogame.repository.UserTypeRepository;

@Service
public class RuralProducerService {
    
    @Autowired
    private RuralProducerRepository ruralProducerRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserDocumentRepository userDocumentRepository;
    
    @Autowired
    private UserDocumentTypeRepository userDocumentTypeRepository;
    
    @Autowired
    private AuthCredentialRepository authCredentialRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserTypeRepository userTypeRepository;
    
    @Autowired
    private UserStatusRepository userStatusRepository;
    
    @Transactional
    public User registerRuralProducer(RuralProducerDTO dto) {
        
        // 1. Buscar empresa pelo ID e validar se está ATIVA (code = 1)
        Company company = companyRepository.findById(dto.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));
        
        // Verifica se a empresa tem status APPROVED (code = 1)
        if (company.getCompanyStatus() == null || 
            company.getCompanyStatus().getCode().equals(EnumCompanyStatus.APPROVED.getId().toString())) {
            throw new IllegalArgumentException("Empresa parceira não está ativa para receber produtores");
        }
        
        // 2. Buscar o tipo de documento selecionado
        UserDocumentType documentType = userDocumentTypeRepository.findById(dto.getDocumentTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de documento não encontrado"));
        
        // 3. Criar entidade User (Produtor Rural)
        User producer = new User();
        producer.setFullName(dto.getFullName());
        producer.setEmail1(dto.getEmail());
        
        UserType userType = userTypeRepository.findByCode(EnumUserType.USER.getCode())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de usuário não encontrado"));
        
        producer.setUserType(userType);
        
        UserStatus pendingStatus = userStatusRepository.findByCode(EnumUserStatus.PENDING.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Status PENDING não encontrado"));
            
        
        producer.setUserStatus(pendingStatus);
        
        // Localização
        producer.setState(dto.getState());
        producer.setAddress(dto.getAddress());
        producer.setNumber(dto.getNumber());
        producer.setCity(dto.getCity());
        producer.setZipcode(dto.getZipcode());
        
        // Relacionamentos
        producer.setCompany(company);
        // TODO: setar userType e userStatus quando souber os IDs
        
        // Auditoria
        producer.setCreatedAt(LocalDateTime.now());
        producer.setPointsBalance(0);
        
        // 4. Salvar produtor no banco
        User savedProducer = ruralProducerRepository.save(producer);
        
        // 5. Criar credenciais de autenticação em auth_credentials
        AuthCredential credential = new AuthCredential();
        credential.setUser(savedProducer);
        credential.setProvider("email"); // Provider padrão para cadastro por email
        credential.setIdentifier(dto.getEmail()); // Email como identificador
        credential.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // Senha criptografada
        credential.setIsActive(true);
        credential.setFailedAttempts(0);
        credential.setCreatedAt(LocalDateTime.now());
        
        // 6. Salvar credenciais
        authCredentialRepository.save(credential);
        
        // 7. Criar registro de documento em user_documents
        UserDocument document = new UserDocument();
        document.setUser(savedProducer);
        document.setDocumentType(documentType);
        document.setDocumentNumber(dto.getDocumentNumber().replaceAll("[^0-9A-Za-z]", "")); // Remove formatação
        document.setIsPrimary(true);
        document.setIsActive(true);
        document.setCreatedAt(LocalDateTime.now());
        document.setCreatedBy(null); 

        String[] parts = dto.getFullName().trim().split("\\s+", 2);
     	producer.setFirstName(parts[0]);
     	producer.setLastName(parts.length > 1 ? parts[1] : "");
        
        // 8. Salvar documento
        userDocumentRepository.save(document);
        
        // 9. TODO: Enviar emails (próxima task)
        // emailService.sendWelcomeEmailToProducer(savedProducer);
        // emailService.sendNewProducerNotificationToCompany(savedProducer, company);
        
        return savedProducer;
    }
    
    /**
     * Aprova um produtor rural (User), mudando status de PENDING para APPROVED
     * @param userId ID do usuário a aprovar
     * @param userEmail Email do usuário autenticado
     * @return User atualizado
     */
    @Transactional
    public User associateProducer(Long userId, String userEmail) {
        // 1. Buscar usuário autenticado (admin)
        User admin = ruralProducerRepository.findByEmail1(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));

        // 2. Buscar produtor rural pelo ID
        User producer = ruralProducerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Produtor rural não encontrado com ID: " + userId));

        if (admin.getCompany() == null || producer.getCompany() == null ||
            !admin.getCompany().getId().equals(producer.getCompany().getId())) {
            throw new IllegalStateException("Você só pode aprovar produtores da sua empresa!");
        }

        // 4. Validar se status está PENDING
        if (!producer.getUserStatus().getCode().equals(EnumUserStatus.PENDING.getCode())) {
            throw new IllegalStateException("Somente usuários com status PENDING podem ser aprovados. Status atual: "
                    + producer.getUserStatus().getCode());
        }

        // 5. Buscar status APPROVED
        UserStatus approvedStatus = userStatusRepository
            .findByCode(EnumUserStatus.APPROVED.getCode())
            .orElseThrow(() -> new RuntimeException("Status 'approved' não encontrado"));

        // 6. Atualizar status e auditoria
        producer.setUserStatus(approvedStatus);
        producer.setUpdatedAt(LocalDateTime.now());
        producer.setUpdatedBy(admin); // Quem aprovou

        // 7. Salvar
        User savedProducer = ruralProducerRepository.save(producer);
        return savedProducer;
    }

}
