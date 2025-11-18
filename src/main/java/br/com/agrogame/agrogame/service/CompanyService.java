package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.CompanyDocumentDTO;
import br.com.agrogame.agrogame.dto.CompanyListDTO;
import br.com.agrogame.agrogame.dto.CompanyTypeDTO;
import br.com.agrogame.agrogame.dto.CreateCompanyDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.BadRequestException;
import br.com.agrogame.agrogame.exceptions.DuplicateResourceException;
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
	private CompanyDocumentsRepository companyDocumentRepository;

	@Autowired
	private CompanyDocumentTypeRepository companyDocumentTypeRepository;

	@Autowired
	private UserTypeRepository userTypeRepository;

	@Autowired
	private UserStatusRepository userStatusRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ValidationService validationService;

	/**
	 * Cadastra uma nova empresa com usuário administrador padrão
	 * 
	 * @param dto           DTO com dados da empresa
	 * @param cnpj          CNPJ extraído dos documentos
	 * @param adminPassword Senha do usuário administrador
	 * @return Company salva no banco
	 */
	@Transactional
	public Company registerCompany(CreateCompanyDTO dto, String cnpj) {

		if (validationService.cnpjAlreadyExists(cnpj)) {
			throw new DuplicateResourceException("CNPJ já cadastrado");
		}
		if (!validationService.isValidEmailFormat(dto.getEmail1())) {
			throw new BadRequestException("Formato de e-mail inválido");
		}
		if (validationService.emailAlreadyExists(dto.getEmail1())) {
			throw new DuplicateResourceException("E-mail já cadastrado");
		}
		// 1. Criar e salvar Company (status PENDING)
		Company company = fromDto(dto);
		Company savedCompany = companyRepository.save(company);

		// 2. Criar usuário administrador padrão
		User adminUser = createDefaultAdminUser(dto, savedCompany);
		User savedAdmin = userRepository.save(adminUser);

		// 3. Atualizar createdBy da Company para o admin
		savedCompany.setCreatedBy(savedAdmin);
		companyRepository.save(savedCompany);

		// 4. Criar AuthCredential para o admin (email corporativo + senha)
		AuthCredential credential = new AuthCredential();
		credential.setUser(savedAdmin);
		credential.setProvider("local");
		credential.setIdentifier(dto.getEmail1()); // Email corporativo da empresa
		credential.setPasswordHash(passwordEncoder.encode(dto.getAdminPassword()));
		credential.setIsActive(true);
		credential.setFailedAttempts(0);
		credential.setCreatedAt(LocalDateTime.now());
		authCredentialRepository.save(credential);

		// 5. Salvar documentos da empresa
		for (CompanyDocumentDTO docDTO : dto.getDocumentos()) {
			CompanyDocument doc = new CompanyDocument();
			doc.setCompany(savedCompany);

			CompanyDocumentType docType = companyDocumentTypeRepository.findByCode(docDTO.getDocument().toString())
					.orElseThrow(() -> new IllegalArgumentException(
							"Tipo de documento não encontrado: " + docDTO.getDocument()));

			doc.setDocumentType(docType);
			doc.setDocumentNumber(docDTO.getDocumentNumber());
			doc.setIsPrimary(docDTO.isPrimary());
			doc.setCreatedAt(LocalDateTime.now());

			companyDocumentRepository.save(doc);
		}

		return savedCompany;
	}

	/**
	 * Cria usuário administrador padrão para a empresa
	 */
	private User createDefaultAdminUser(CreateCompanyDTO dto, Company company) {
		User user = new User();

		// Nome completo do responsável
		user.setFullName(dto.getResponsibleName());

		// Dividir nome em firstName e lastName
		String[] parts = dto.getResponsibleName().trim().split("\\s+", 2);
		user.setFirstName(parts[0]);
		user.setLastName(parts.length > 1 ? parts[1] : "");

		// Email corporativo da empresa como email do usuário
		user.setEmail1(dto.getEmail1());
		user.setEmail2(dto.getEmail2());

		// Vinculação com company
		user.setCompany(company);

		// UserType: administrador
		UserType adminType = userTypeRepository.findByCode(EnumUserType.ADMINISTRATOR.getCode()).orElseThrow(
				() -> new RuntimeException("UserType '" + EnumUserType.ADMINISTRATOR.getCode() + "' não encontrado"));
		user.setUserType(adminType);

		// UserStatus: ativo
		UserStatus activeStatus = userStatusRepository.findByCode(EnumUserStatus.ACTIVE.getCode()).orElseThrow(
				() -> new RuntimeException("UserStatus '" + EnumUserStatus.ACTIVE.getCode() + "' não encontrado"));
		user.setUserStatus(activeStatus);

		user.setCreatedAt(LocalDateTime.now());

		return user;
	}

	public Company fromDto(CreateCompanyDTO dto) {
		Company company = new Company();
		company.setFullCompanyName(dto.getFullCompanyName());
		company.setFantansyName(dto.getFantasyName());
		company.setEmail1(dto.getEmail1()); // Email corporativo
		company.setEmail2(dto.getEmail2());
		company.setPhone1(dto.getPhone1());
		company.setPhone2(dto.getPhone2());
		company.setAddress(dto.getAddress());
		company.setCity(dto.getCity());
		company.setState(dto.getState());
		company.setCountry(dto.getCountry());
		company.setResponsibleName(dto.getResponsibleName());
		company.setResponsiblePhone(dto.getResponsiblePhone());

		CompanyStatus status = companyStatusRepository.findByCode(EnumCompanyStatus.PENDING.getCode())
				.orElseThrow(() -> new IllegalArgumentException("Status pending não encontrado"));
		company.setCompanyStatus(status);

		CompanyType companyType = companyTypeRepository.findById(dto.getCompanyTypeId())
				.orElseThrow(() -> new IllegalArgumentException("Tipo de empresa não encontrado"));
		company.setCompanyType(companyType);

		company.setCreatedAt(LocalDateTime.now());

		return company;
	}

	/**
	 * Lista todas as empresas
	 */
	public List<Company> findAll() {
		return companyRepository.findAllWithStatusAndType();
	}

	public List<CompanyTypeDTO> findAllCompanyTypes(String status) {
		Boolean isActive = null;
		if ("active".equalsIgnoreCase(status)) {
			isActive = true;
		} else if ("inactive".equalsIgnoreCase(status)) {
			isActive = false;
		}

		List<CompanyType> list = companyTypeRepository.findAllByIsActive(isActive);

		return list.stream().map(CompanyTypeDTO::new).toList();
	}

	/**
	 * Aprova uma empresa, mudando status de PENDING para APPROVED
	 * 
	 * @param companyId ID da empresa
	 * @param userEmail Email do usuário autenticado
	 * @return Company atualizada
	 */
	@Transactional
	public Company approveCompany(Integer companyId, String userEmail) {
		// 1. Buscar usuário
		User user = userRepository.findByEmail1(userEmail)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		// 3. Buscar empresa
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new RuntimeException("Empresa não encontrada com ID: " + companyId));

		// 4. Validar se está PENDING
		if (!company.getCompanyStatus().getCode().equals(EnumCompanyStatus.PENDING.getCode())) {
			throw new IllegalStateException("Somente empresas com status PENDING podem ser aprovadas. Status atual: "
					+ company.getCompanyStatus().getCode());
		}

		// 5. Buscar status APPROVED
		CompanyStatus approvedStatus = companyStatusRepository.findByCode(EnumCompanyStatus.APPROVED.getCode())
				.orElseThrow(() -> new RuntimeException("Status 'approved' não encontrado"));

		// 6. Atualizar status
		company.setCompanyStatus(approvedStatus);
		company.setUpdatedAt(LocalDateTime.now());
		company.setUpdatedBy(user); // Registrar quem aprovou

		// 7. Salvar
		Company savedCompany = companyRepository.save(company);

		return savedCompany;
	}

	public List<Company> findAllActive() {
		return companyRepository.findActiveCompanies(EnumCompanyStatus.APPROVED.getCode());
	}

	public List<CompanyListDTO> findCompanies(String status, Integer companyTypeId) {
		List<Company> companies = companyRepository.findByStatusAndTypeFetch(status, companyTypeId);
		return companies.stream().map(CompanyListDTO::new).toList();
	}

}