package br.com.agrogame.agrogame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.ActivityCropTypeProjection;
import br.com.agrogame.agrogame.dto.ProducerActivityByFarmDTO;
import br.com.agrogame.agrogame.dto.ProducerActivityDTO;
import br.com.agrogame.agrogame.dto.ProducerUserActivityProjection;
import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.BadRequestException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.DuplicateResourceException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.FarmCrop;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocument;
import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.FarmCropRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.RuralProducerRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserDocumentTypeRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
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

	@Autowired
	private ValidationService validationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FarmRepository farmRepository;

	@Autowired
	private FarmCropRepository farmCropRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private ActivityCropTypeRepository activityCropTypeRepository;

	@Autowired
	private UserActivityRepository userActivityRepository;

	@Transactional
	public User registerRuralProducer(RuralProducerDTO dto) {

		// 1. Buscar empresa pelo ID e validar se está ATIVA (code = 1)
		Company company = companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

		// Verifica se a empresa tem status APPROVED (code = 1)
		if (company.getCompanyStatus() == null
				|| company.getCompanyStatus().getCode().equals(EnumCompanyStatus.APPROVED.getId().toString())) {
			throw new BusinessException("Empresa parceira não está ativa para receber produtores");
		}

		// 2. Buscar o tipo de documento selecionado
		UserDocumentType documentType = userDocumentTypeRepository.findById(dto.getDocumentTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de documento não encontrado"));

		// 3. Criar entidade User (Produtor Rural)
		User producer = new User();
		producer.setFullName(dto.getFullName());
		producer.setEmail1(dto.getEmail());

		UserType userType = userTypeRepository.findByCode(EnumUserType.PRODUCER.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário não encontrado"));

		producer.setUserType(userType);

		UserStatus pendingStatus = userStatusRepository.findByCode(EnumUserStatus.PENDING.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status PENDING não encontrado"));

		if (!validationService.isValidEmailFormat(dto.getEmail())) {
			throw new BadRequestException("Formato de e-mail inválido");
		}
		if (validationService.emailAlreadyExists(dto.getEmail())) {
			throw new DuplicateResourceException("E-mail já cadastrado");
		}
		if (validationService.documentAlreadyExists(dto.getDocumentTypeId(), dto.getDocumentNumber())) {
			throw new DuplicateResourceException("Documento já cadastrado");
		}

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
	 * 
	 * @param userId    ID do usuário a aprovar
	 * @param userEmail Email do usuário autenticado
	 * @return User atualizado
	 */
	@Transactional
	public User associateProducer(Long userId, String userEmail) {
		// 1. Buscar usuário autenticado (admin)
		User admin = ruralProducerRepository.findByEmail1(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

		// 2. Buscar produtor rural pelo ID
		User producer = ruralProducerRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor rural não encontrado com ID: " + userId));

		if (admin.getCompany() == null || producer.getCompany() == null
				|| !admin.getCompany().getId().equals(producer.getCompany().getId())) {
			throw new BusinessException("Você só pode aprovar produtores da sua empresa!");
		}

		// 4. Validar se status está PENDING
		if (!producer.getUserStatus().getCode().equals(EnumUserStatus.PENDING.getCode())) {
			throw new BusinessException("Somente usuários com status PENDING podem ser aprovados. Status atual: "
					+ producer.getUserStatus().getCode());
		}

		// 5. Buscar status APPROVED
		UserStatus approvedStatus = userStatusRepository.findByCode(EnumUserStatus.APPROVED.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status 'approved' não encontrado"));

		// 6. Atualizar status e auditoria
		producer.setUserStatus(approvedStatus);
		producer.setUpdatedAt(LocalDateTime.now());
		producer.setUpdatedBy(admin); // Quem aprovou

		// 7. Salvar
		User savedProducer = ruralProducerRepository.save(producer);
		return savedProducer;
	}

	@Transactional(readOnly = true)
	public List<ProducerActivityByFarmDTO> listActivitiesForProducer(Integer producerId, Integer farmIdFilter,
			Integer cropTypeIdFilter, String nameFilter, LocalDate validFromStart, LocalDate validFromEnd,
			LocalDate validToStart, LocalDate validToEnd) {

		// 1. Buscar produtor
		User producer = userRepository.findByIdWithUserType(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		// 2. Validar que é produtor
		if (producer.getUserType().getId() != 8) {
			throw new BusinessException("Usuário não é produtor rural");
		}

		Integer companyId = producer.getCompany().getId();

		// 3. Buscar fazendas do produtor (opcionalmente filtrar por farmIdFilter)
		List<Farm> farms;
		if (farmIdFilter != null) {
			Farm farm = farmRepository.findById(farmIdFilter)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(producerId) || !farm.getCompany().getId().equals(companyId)) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
			farms = List.of(farm);
		} else {
			farms = farmRepository.findByOwnerIdAndCompanyId(producerId, companyId);
		}

		if (farms.isEmpty()) {
			return List.of();
		}

		// 4. Para cada fazenda, buscar atividades compatíveis
		List<ProducerActivityByFarmDTO> result = new ArrayList<>();

		for (Farm farm : farms) {
			// Buscar atividades desta fazenda com suas culturas
			List<Activity> activities = activityRepository.findActivitiesForFarm(farm.getId(), companyId);
			if (activities.isEmpty()) {
				continue;
			}

			// Se filtro de cropTypeId enviado, filtrar apenas atividades com esse crop
			if (cropTypeIdFilter != null) {
				activities = activities.stream()
						.filter(activity -> activityCropTypeRepository.findByActivityId(activity.getId()).stream()
								.anyMatch(act -> act.getCropType().getId().equals(cropTypeIdFilter)))
						.toList();
				if (activities.isEmpty()) {
					continue;
				}
			}

			// Filtro de datas
			activities = activities.stream().filter(
					activity -> applyDateFilter(activity, validFromStart, validFromEnd, validToStart, validToEnd))
					.toList();
			if (activities.isEmpty()) {
				continue;
			}

			activities = activities.stream().filter(activity -> {
				if (nameFilter == null || nameFilter.isBlank()) {
					return true;
				}
				String name = activity.getName();
				return name != null && name.toLowerCase().contains(nameFilter.toLowerCase());
			}).toList();

			if (activities.isEmpty()) {
				continue;
			}

			// Ordenar pela data de expiração
			activities = activities.stream().sorted(Comparator.comparing(Activity::getValidTo)).toList();

			// Buscar culturas da fazenda para agrupamento
			List<FarmCrop> farmCrops = farmCropRepository.findByFarmId(farm.getId());

			// Montar DTO para cada farm/crop
			for (FarmCrop farmCrop : farmCrops) {
				ProducerActivityByFarmDTO farmDto = new ProducerActivityByFarmDTO(farm.getId(), farm.getName(),
						farmCrop.getCropType().getName());

				List<ProducerActivityDTO> activitiesForCrop = activities.stream()
						.filter(activity -> activityCropTypeRepository.findByActivityId(activity.getId()).stream()
								.anyMatch(act -> act.getCropType().getId().equals(farmCrop.getCropType().getId())))
						.map(this::toProducerActivityDTO).toList();

				if (!activitiesForCrop.isEmpty()) {
					farmDto.setActivities(activitiesForCrop);
					result.add(farmDto);
				}
			}
		}

		return result;
	}

	private boolean applyDateFilter(Activity activity, LocalDate validFromStart, LocalDate validFromEnd,
			LocalDate validToStart, LocalDate validToEnd) {

		if (validFromStart != null && activity.getValidFrom().isBefore(validFromStart)) {
			return false;
		}
		if (validFromEnd != null && activity.getValidFrom().isAfter(validFromEnd)) {
			return false;
		}
		if (validToStart != null && activity.getValidTo().isBefore(validToStart)) {
			return false;
		}
		if (validToEnd != null && activity.getValidTo().isAfter(validToEnd)) {
			return false;
		}

		return true;
	}

	private ProducerActivityDTO toProducerActivityDTO(Activity activity) {
		ProducerActivityDTO dto = new ProducerActivityDTO();
		dto.setActivityId(activity.getId());
		dto.setName(activity.getName());
		dto.setDescription(activity.getDescription());
		dto.setPoints(activity.getPoints());
		dto.setValidFrom(activity.getValidFrom());
		dto.setValidTo(activity.getValidTo());
		dto.setStatus(activity.getActivityStatus().getCode());
		dto.setCompanyName(activity.getCompany().getFantasyName());

		// Buscar crop types dessa atividade
		List<String> cropTypeNames = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(act -> act.getCropType().getName()).toList();

		dto.setCropTypes(cropTypeNames);

		return dto;
	}

	@Transactional(readOnly = true)
	public Page<ProducerActivityDTO> listActivitiesForProducerFast(Integer producerId, Integer farmId,
			Integer cropTypeIdFilter, String nameFilter, LocalDate validFromStart, LocalDate validFromEnd,
			LocalDate validToStart, LocalDate validToEnd, String status, Integer unidadeProdutivaId, int page, int size) {
		User producer = userRepository.findByIdWithUserType(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		if (producer.getUserType() == null || producer.getUserType().getId() != 8) {
			throw new BusinessException("Usuário não é produtor rural");
		}

		if (producer.getCompany() == null) {
			throw new BusinessException("Produtor não vinculado a uma empresa");
		}

		Integer companyId = producer.getCompany().getId();

		// farms do produtor
		List<Integer> farmIds;
		if (farmId != null) {
			Farm farm = farmRepository.findById(farmId)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

			if (!farm.getOwner().getId().equals(producerId) || !farm.getCompany().getId().equals(companyId)) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}

			farmIds = List.of(farmId);
		} else {
			farmIds = farmRepository.findByOwnerIdAndCompanyId(producerId, companyId).stream().map(Farm::getId)
					.toList();

			if (farmIds.isEmpty()) {
				return Page.empty();
			}
		}

		// paginação (ordenando por validTo asc, como você já faz no sorted)
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "validTo"));

		Page<ProducerUserActivityProjection> rowsPage = userActivityRepository.listUserActivitiesForProducer(companyId,
				producerId, farmIds, status, validFromStart, validFromEnd, cropTypeIdFilter, unidadeProdutivaId, pageable);

		if (rowsPage.isEmpty()) {
			return Page.empty(pageable);
		}

		// crops por Activity
		List<Integer> activityIds = rowsPage.getContent().stream().map(ProducerUserActivityProjection::getActivityId)
				.distinct().toList();

		Map<Integer, List<String>> activityCropsMap = activityCropTypeRepository.findCropsByActivityIds(activityIds)
				.stream().collect(Collectors.groupingBy(ActivityCropTypeProjection::getActivityId,
						Collectors.mapping(ActivityCropTypeProjection::getCropName, Collectors.toList())));

		// mapear para DTO
		Page<ProducerActivityDTO> dtoPage = rowsPage.map(row -> {
			ProducerActivityDTO dto = new ProducerActivityDTO();
			dto.setActivityId(row.getActivityId());
			dto.setName(row.getActivityName());
			dto.setDescription(row.getDescription());
			dto.setPoints(row.getPoints());
			dto.setValidFrom(row.getValidFrom());
			dto.setValidTo(row.getValidTo());
			dto.setStatus(row.getActivityStatus());
			dto.setCompanyName(producer.getCompany().getFantasyName());

			List<String> cropTypeNames = activityCropsMap.getOrDefault(row.getActivityId(), List.of());
			dto.setCropTypes(cropTypeNames);

			dto.setUserActivityId(row.getUserActivityId());
			dto.setUserActivityStatus(row.getUserActivityStatus());
			dto.setUserActivityFarmId(row.getUserActivityFarmId());
			dto.setProductionUnitId(row.getProductionUnitId());
			dto.setThumbnailUrl(row.getThumbnailUrl());
			dto.setThumbnailGsutilUri(row.getThumbnailGsutilUri());

			return dto;
		});

		// filtros adicionais (name, validToStart/End) ainda estão em memória
		if ((nameFilter != null && !nameFilter.isBlank()) || validToStart != null || validToEnd != null) {
			List<ProducerActivityDTO> filtered = dtoPage.getContent().stream().filter(dto -> {
				if (nameFilter == null || nameFilter.isBlank())
					return true;
				String n = dto.getName();
				return n != null && n.toLowerCase().contains(nameFilter.toLowerCase());
			}).filter(dto -> {
				if (validToStart != null && dto.getValidTo().isBefore(validToStart))
					return false;
				if (validToEnd != null && dto.getValidTo().isAfter(validToEnd))
					return false;
				return true;
			}).sorted(Comparator.comparing(ProducerActivityDTO::getValidTo)).toList();

			return new PageImpl<>(filtered, pageable, dtoPage.getTotalElements());
		}

		return dtoPage;
	}

}
