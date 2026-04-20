package br.com.agrogame.agrogame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.ActivityCropTypeProjection;
import br.com.agrogame.agrogame.dto.NotificationEvent;
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
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocument;
import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.model.WorkerProductionUnitAssignment;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.FarmCropRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
import br.com.agrogame.agrogame.repository.RuralProducerRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserDocumentTypeRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.repository.UserStatusRepository;
import br.com.agrogame.agrogame.repository.UserTypeRepository;
import br.com.agrogame.agrogame.repository.WorkerProductionUnitAssignmentRepository;

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

	@Autowired
	private NotificationPublisherService notificationPublisherService;
	
	@Autowired
	private ProductionUnitRepository productionUnitRepository;

	@Autowired
	private WorkerProductionUnitAssignmentRepository assignmentRepository;
	
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
		publishProducerPendingApprovalEvent(savedProducer);

		return savedProducer;
	}

	private void publishProducerPendingApprovalEvent(User producer) {
		Map<String, Object> vars = new HashMap<>();
		vars.put("tipoPerfil", "PRODUTOR");
		vars.put("nomeProdutor", producer.getFullName());

		NotificationEvent event = new NotificationEvent("AGUARDANDO_APROVACAO", producer.getEmail1(), vars);

		notificationPublisherService.publishNotification(event, producer.getId());
	}

	/**
	 * Aprova um produtor rural (User), mudando status de PENDING para APPROVED
	 * 
	 * @param userId    ID do usuário a aprovar
	 * @param userEmail Email do usuário autenticado
	 * @return User atualizado
	 */
	@Transactional
	public User processAssociation(Long userId, String userEmail, String action) {
		User admin = ruralProducerRepository.findByEmail1(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
		User producer = ruralProducerRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor rural não encontrado: " + userId));

		boolean isSuperAdmin = admin.getUserType().getCode().equals(EnumUserType.SUPERADMIN.getCode());

		if (!isSuperAdmin) {
			if (admin.getCompany() == null || producer.getCompany() == null
					|| !admin.getCompany().getId().equals(producer.getCompany().getId())) {
				throw new BusinessException("Você só pode aprovar produtores da sua empresa!");
			}
		}

		if (!producer.getUserStatus().getCode().equals(EnumUserStatus.PENDING.getCode())) {
			throw new BusinessException(
					"Somente PENDING pode ser processado. Status: " + producer.getUserStatus().getCode());
		}

		// Buscar status pelo action
		UserStatus targetStatus;
		switch (action.toUpperCase()) {
		case "APPROVED" -> targetStatus = userStatusRepository.findByCode(EnumUserStatus.APPROVED.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status APPROVED não encontrado"));
		case "REJECTED" -> targetStatus = userStatusRepository.findByCode(EnumUserStatus.REJECTED.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status REJECTED não encontrado"));
		default -> throw new BusinessException("Ação inválida: " + action + ". Use APPROVE ou REJECT.");
		}

		// 6-7. Atualizar e salvar (mesmo para approve/reject)
		producer.setUserStatus(targetStatus);
		producer.setUpdatedAt(LocalDateTime.now());
		producer.setUpdatedBy(admin);
		producer.setApprovedAt(LocalDateTime.now());
		producer.setApprovedBy(admin);
		User saved = ruralProducerRepository.save(producer);

		if ("APPROVED".equalsIgnoreCase(action)) {
			publishProducerApprovedEvent(saved);
		}
//		else if ("REJECT".equalsIgnoreCase(action)) {
//			publishProducerRejectedEvent(saved);
//		}

		return saved;
	}

	private void publishProducerApprovedEvent(User producer) {
		// Buscar CPF do produtor em user_documents
		String cpf = userDocumentRepository.findFirstByUserIdAndDocumentType_Code(producer.getId(), "cpf")
				.map(UserDocument::getDocumentNumber).orElse(null);

		Map<String, Object> vars = new HashMap<>();
		vars.put("tipoPerfil", "PRODUTOR");
		vars.put("nomeProdutor", producer.getFullName());
		vars.put("cpf", cpf);
		vars.put("emailProdutor", producer.getEmail1());

		NotificationEvent event = new NotificationEvent("CADASTRO_APROVADO", producer.getEmail1(), vars);

		notificationPublisherService.publishNotification(event, producer.getId());
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
	        LocalDate validToStart, LocalDate validToEnd, String status, Integer unidadeProdutivaId, int page,
	        int size) {

	    User user = userRepository.findByIdWithUserType(producerId)
	            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

	    if (user.getUserType() != null && user.getUserType().getId() == 9) {
	        return listActivitiesForWorkerFast(user, nameFilter, validFromStart, validFromEnd,
	                validToStart, validToEnd, status, page, size);
	    }

	    User producer = user;

	    if (producer.getUserType() == null || producer.getUserType().getId() != 8) {
	        throw new BusinessException("Usuário não é produtor rural");
	    }

	    if (producer.getCompany() == null) {
	        throw new BusinessException("Produtor não vinculado a uma empresa");
	    }

	    Integer companyId = producer.getCompany().getId();

	    List<Integer> farmIds;
	    if (farmId != null) {
	        Farm farm = farmRepository.findById(farmId)
	                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

	        if (!farm.getOwner().getId().equals(producerId) || !farm.getCompany().getId().equals(companyId)) {
	            throw new BusinessException("Fazenda não pertence ao produtor");
	        }

	        farmIds = List.of(farmId);
	    } else {
	        farmIds = farmRepository.findByOwnerIdAndCompanyId(producerId, companyId)
	                .stream()
	                .map(Farm::getId)
	                .toList();

	        if (farmIds.isEmpty()) {
	            return Page.empty();
	        }
	    }

	    Pageable pageable = PageRequest.of(page, size);

	    Page<ProducerUserActivityProjection> rowsPage = userActivityRepository.listUserActivitiesForProducer(
	            companyId, producerId, farmIds, status, validFromStart, validFromEnd,
	            cropTypeIdFilter, unidadeProdutivaId, pageable);

	    if (rowsPage.isEmpty()) {
	        return Page.empty(pageable);
	    }

	    List<Integer> activityIds = rowsPage.getContent().stream()
	            .map(ProducerUserActivityProjection::getActivityId)
	            .distinct()
	            .toList();

	    Map<Integer, List<String>> activityCropsMap = activityCropTypeRepository.findCropsByActivityIds(activityIds)
	            .stream()
	            .collect(Collectors.groupingBy(
	                    ActivityCropTypeProjection::getActivityId,
	                    Collectors.mapping(ActivityCropTypeProjection::getCropName, Collectors.toList())
	            ));

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

	    if ((nameFilter != null && !nameFilter.isBlank()) || validToStart != null || validToEnd != null) {
	        List<ProducerActivityDTO> filtered = dtoPage.getContent().stream()
	                .filter(dto -> {
	                    if (nameFilter == null || nameFilter.isBlank()) return true;
	                    String n = dto.getName();
	                    return n != null && n.toLowerCase().contains(nameFilter.toLowerCase());
	                })
	                .filter(dto -> {
	                    if (validToStart != null && dto.getValidTo().isBefore(validToStart)) return false;
	                    if (validToEnd != null && dto.getValidTo().isAfter(validToEnd)) return false;
	                    return true;
	                })
	                .sorted(Comparator.comparing(ProducerActivityDTO::getValidTo))
	                .toList();

	        return new PageImpl<>(filtered, pageable, dtoPage.getTotalElements());
	    }

	    return dtoPage;
	}
	
	@Transactional(readOnly = true)
	public Page<ProducerActivityDTO> listActivitiesForWorkerFast(User worker, String nameFilter,
	        LocalDate validFromStart, LocalDate validFromEnd,
	        LocalDate validToStart, LocalDate validToEnd,
	        String status, int page, int size) {

	    if (worker.getUserType() == null || worker.getUserType().getId() != 9) {
	        throw new BusinessException("Usuário não é worker");
	    }

	    if (worker.getCompany() == null) {
	        throw new BusinessException("Worker não vinculado a uma empresa");
	    }

	    Integer companyId = worker.getCompany().getId();

	    WorkerProductionUnitAssignment assignment = assignmentRepository.findFirstByWorkerIdAndIsActiveTrue(worker.getId())
	            .orElseThrow(() -> new ResourceNotFoundException("Worker sem unidade produtiva ativa"));

	    Integer productionUnitId = assignment.getProductionUnitId();

	    ProductionUnit productionUnit = productionUnitRepository.findById(productionUnitId)
	            .orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

	    Integer farmId = productionUnit.getFarm().getId();
	    Integer cropTypeId = productionUnit.getCropType() != null ? productionUnit.getCropType().getId() : null;

	    Farm farm = farmRepository.findById(farmId)
	            .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

	    if (farm.getCompany() == null || !farm.getCompany().getId().equals(companyId)) {
	        throw new BusinessException("Fazenda da unidade produtiva não pertence à empresa do worker");
	    }

	    Pageable pageable = PageRequest.of(page, size);

	    Page<ProducerUserActivityProjection> rowsPage = userActivityRepository.listUserActivitiesForWorker(
	            companyId, farmId, status, validFromStart, validFromEnd,
	            cropTypeId, productionUnitId, pageable);

	    if (rowsPage.isEmpty()) {
	        return Page.empty(pageable);
	    }

	    List<Integer> activityIds = rowsPage.getContent().stream()
	            .map(ProducerUserActivityProjection::getActivityId)
	            .distinct()
	            .toList();

	    Map<Integer, List<String>> activityCropsMap = activityCropTypeRepository.findCropsByActivityIds(activityIds)
	            .stream()
	            .collect(Collectors.groupingBy(
	                    ActivityCropTypeProjection::getActivityId,
	                    Collectors.mapping(ActivityCropTypeProjection::getCropName, Collectors.toList())
	            ));

	    Page<ProducerActivityDTO> dtoPage = rowsPage.map(row -> {
	        ProducerActivityDTO dto = new ProducerActivityDTO();
	        dto.setActivityId(row.getActivityId());
	        dto.setName(row.getActivityName());
	        dto.setDescription(row.getDescription());
	        dto.setPoints(row.getPoints());
	        dto.setValidFrom(row.getValidFrom());
	        dto.setValidTo(row.getValidTo());
	        dto.setStatus(row.getActivityStatus());
	        dto.setCompanyName(worker.getCompany().getFantasyName());

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

	    if ((nameFilter != null && !nameFilter.isBlank()) || validToStart != null || validToEnd != null) {
	        List<ProducerActivityDTO> filtered = dtoPage.getContent().stream()
	                .filter(dto -> {
	                    if (nameFilter == null || nameFilter.isBlank()) return true;
	                    String n = dto.getName();
	                    return n != null && n.toLowerCase().contains(nameFilter.toLowerCase());
	                })
	                .filter(dto -> {
	                    if (validToStart != null && dto.getValidTo().isBefore(validToStart)) return false;
	                    if (validToEnd != null && dto.getValidTo().isAfter(validToEnd)) return false;
	                    return true;
	                })
	                .sorted(Comparator.comparing(ProducerActivityDTO::getValidTo))
	                .toList();

	        return new PageImpl<>(filtered, pageable, dtoPage.getTotalElements());
	    }

	    return dtoPage;
	}
}
