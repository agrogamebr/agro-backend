package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.ActivityListDTO;
import br.com.agrogame.agrogame.dto.CreateActivityDTO;
import br.com.agrogame.agrogame.dto.CreateActivityMultipartDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityCropType;
import br.com.agrogame.agrogame.model.ActivityReward;
import br.com.agrogame.agrogame.model.ActivityStatus;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.FarmCrop;
import br.com.agrogame.agrogame.model.Reward;
import br.com.agrogame.agrogame.model.RewardStatus;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.ActivityRewardRepository;
import br.com.agrogame.agrogame.repository.ActivityStatusRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.FarmCropRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.RewardRepository;
import br.com.agrogame.agrogame.repository.RewardStatusRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserActivityStatusRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.StoredFileInfo;
import jakarta.transaction.Transactional;

@Service
public class ActivityService {

	private final ActivityRepository activityRepository;
	private final CompanyRepository companyRepository;
	private final ActivityStatusRepository activityStatusRepository;
	private final ActivityRewardRepository activityRewardRepository;
	private final RewardRepository rewardRepository;
	private final RewardStatusRepository rewardStatusRepository;
	private final CropTypeRepository cropTypeRepository;
	private final ActivityCropTypeRepository activityCropTypeRepository;
	private final UserActivityRepository userActivityRepository;
	private final UserActivityStatusRepository userActivityStatusRepository;
	private final FarmRepository farmRepository;
	private final FarmCropRepository farmCropRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;

	private static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024; // 5MB

	public ActivityService(ActivityRepository activityRepository, CompanyRepository companyRepository,
			ActivityStatusRepository activityStatusRepository, ActivityRewardRepository activityRewardRepository,
			RewardRepository rewardRepository, RewardStatusRepository rewardStatusRepository,
			CropTypeRepository cropTypeRepository, ActivityCropTypeRepository activityCropTypeRepository,
			UserActivityRepository userActivityRepository, UserActivityStatusRepository userActivityStatusRepository,
			FarmRepository farmRepository, FarmCropRepository farmCropRepository, UserRepository userRepository,
			FileStorageService fileStorageService) {
		this.activityRepository = activityRepository;
		this.companyRepository = companyRepository;
		this.activityStatusRepository = activityStatusRepository;
		this.activityRewardRepository = activityRewardRepository;
		this.rewardRepository = rewardRepository;
		this.rewardStatusRepository = rewardStatusRepository;
		this.cropTypeRepository = cropTypeRepository;
		this.activityCropTypeRepository = activityCropTypeRepository;
		this.userActivityRepository = userActivityRepository;
		this.userActivityStatusRepository = userActivityStatusRepository;
		this.farmRepository = farmRepository;
		this.farmCropRepository = farmCropRepository;
		this.userRepository = userRepository;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public Map<String, Object> registerActivity(CreateActivityMultipartDTO dto, Company company, Integer createdBy) throws IOException {

		ActivityStatus draftStatus = activityStatusRepository.findByCode("draft")
				.orElseThrow(() -> new BusinessException("Status 'draft' não configurado"));

		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new BusinessException("Data inicial não pode ser maior que a data final");
		}

		// ========== VALIDAR SE EMPRESA TEM PRODUTORES COM ESSES CROPS ==========
		validateCompanyHasCropTypes(company, dto.getCropTypeIds());

		// ========== CRIAR ACTIVITY ==========
		Activity activity = new Activity();
		activity.setCompany(company);
		activity.setDescription(dto.getDescription());
		activity.setName(dto.getName());
		activity.setPoints(dto.getPoints());
		activity.setActivityStatus(draftStatus);
		activity.setValidFrom(dto.getValidFrom());
		activity.setValidTo(dto.getValidTo());
		activity.setCreatedAt(LocalDateTime.now());
		activity.setCreatedBy(createdBy);

	    if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
	        validateThumbnailFile(dto.getThumbnail());
	        StoredFileInfo stored = fileStorageService.uploadFile(dto.getThumbnail());
	        activity.setThumbnailUrl(stored.getFileUrl());
	        activity.setThumbnailGsutilUri(stored.getGsutilUri());

	    }

		Activity savedActivity = activityRepository.save(activity);

		// ========== VINCULAR CROP TYPES ==========
		for (Integer cropTypeId : dto.getCropTypeIds()) {
			CropType cropType = cropTypeRepository.findById(cropTypeId)
					.orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado: " + cropTypeId));

			ActivityCropType activityCropType = new ActivityCropType();
			activityCropType.setActivity(savedActivity);
			activityCropType.setCropType(cropType);
			activityCropType.setCreatedAt(LocalDateTime.now());
			activityCropType.setCreatedBy(createdBy);

			activityCropTypeRepository.save(activityCropType);
		}

		// ========== CRIAR REWARD ==========
		RewardStatus rewardStatus = rewardStatusRepository.findByCode("active")
				.orElseThrow(() -> new BusinessException("Status de recompensa 'active' não configurado"));

		Reward reward = new Reward();
		reward.setName("Recompensa - " + savedActivity.getDescription());
		reward.setPointsGain(dto.getPoints());
		reward.setDescription("Recompensa gerada automaticamente para a atividade: " + savedActivity.getDescription());
		reward.setValidFrom(dto.getValidFrom());
		reward.setValidTo(dto.getValidTo());
		reward.setPointsCost(dto.getPoints());
		reward.setRewardStatus(rewardStatus);
		reward.setCreatedAt(LocalDateTime.now());
		reward.setCreatedBy(createdBy);

		Reward savedReward = rewardRepository.save(reward);

		// ========== VINCULAR EM ACTIVITY_REWARDS ==========
		ActivityReward activityReward = new ActivityReward();
		activityReward.setActivity(savedActivity);
		activityReward.setReward(savedReward);
		activityReward.setPointsGain(dto.getPoints());
		activityReward.setCreatedAt(LocalDateTime.now());
		activityReward.setCreatedBy(createdBy);

		activityRewardRepository.save(activityReward);

		// ========== RESPONSE ==========
		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("name", savedActivity.getName());
		response.put("companyId", company.getId());
		response.put("description", savedActivity.getDescription());
		response.put("points", savedActivity.getPoints());
		response.put("status", draftStatus.getCode());
		response.put("validFrom", savedActivity.getValidFrom());
		response.put("validTo", savedActivity.getValidTo());
		response.put("cropTypesCount", dto.getCropTypeIds().size());
		response.put("rewardId", savedReward.getId());
	    response.put("thumbnailUrl", savedActivity.getThumbnailUrl());
	    response.put("thumbnailGsutilUri", savedActivity.getThumbnailGsutilUri());
		response.put("message", "Atividade em status 'draft'. Valide e envie com a API de send.");

		return response;
	}

	@Transactional
	public Map<String, Object> sendActivity(Integer activityId, Integer sentBy) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Apenas atividades em draft podem ser enviadas");
		}

		// Mudar status para "send"
		ActivityStatus sendStatus = activityStatusRepository.findByCode("send")
				.orElseThrow(() -> new BusinessException("Status 'send' não configurado"));

		activity.setActivityStatus(sendStatus);
		activity.setUpdatedAt(LocalDateTime.now());
		activity.setUpdatedBy(sentBy);

		Activity savedActivity = activityRepository.save(activity);

		// ========== CRIAR USER_ACTIVITIES PARA FARMS COM ESSES CROPS ==========
		createUserActivitiesForMatchingFarms(savedActivity, sentBy);

		// Notificar producers (se quiser)
		// notifyProducers(savedActivity);

		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("status", sendStatus.getCode());
		response.put("message", "Atividade enviada para os producers!");

		return response;
	}

	/**
	 * Cria UserActivity para todas as farms da empresa que possuem um dos cropTypes
	 * da atividade.
	 */
	private void createUserActivitiesForMatchingFarms(Activity activity, Integer createdBy) {

		UserActivityStatus pendingStatus = userActivityStatusRepository.findByCode("pending")
				.orElseThrow(() -> new BusinessException("Status 'pending' não configurado"));

		User userCreatedBy = userRepository.findById(createdBy)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Company company = activity.getCompany();

		// 1. Buscar todos os cropTypeIds da atividade
		List<Integer> activityCropTypeIds = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(act -> act.getCropType().getId()).toList();

		// 2. Buscar todas as farms da empresa
		List<Farm> companyFarms = farmRepository.findByCompanyIdAndIsActiveTrue(company.getId());

		// 3. Para cada farm, verificar se tem algum dos crops da atividade
		for (Farm farm : companyFarms) {
			List<FarmCrop> farmCrops = farmCropRepository.findByFarmId(farm.getId());

			boolean hasCrop = farmCrops.stream().anyMatch(fc -> activityCropTypeIds.contains(fc.getCropType().getId()));

			if (hasCrop) {
				// 4. Criar UserActivity para o produtor dessa farm
				UserActivity userActivity = new UserActivity();
				userActivity.setActivity(activity);
				userActivity.setUser(farm.getOwner());
				userActivity.setFarm(farm);
				userActivity.setStatus(pendingStatus);
				userActivity.setCreatedAt(LocalDateTime.now());
				userActivity.setCreatedBy(userCreatedBy);

				userActivityRepository.save(userActivity);
			}
		}
	}

	@Transactional
	public Map<String, Object> updateActivity(Integer activityId, CreateActivityDTO dto, Integer updatedBy) {

		// 1. Buscar atividade
		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		// 2. Validar status = draft
		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Atividade não pode ser editada pois não está em status 'draft'");
		}

		// 3. Validar empresa
		Company company = companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

		// 4. Validar datas
		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new BusinessException("Data inicial não pode ser maior que a data final");
		}

		// 5. Atualizar campos básicos
		activity.setCompany(company);
		activity.setDescription(dto.getDescription());
		activity.setPoints(dto.getPoints());
		activity.setValidFrom(dto.getValidFrom());
		activity.setValidTo(dto.getValidTo());
		activity.setUpdatedAt(LocalDateTime.now());
		activity.setUpdatedBy(updatedBy);
		activity.setName(dto.getName());

		Activity savedActivity = activityRepository.save(activity);

		// 6. Atualizar vínculos de crop types
		activityCropTypeRepository.deleteByActivityId(activityId);

		for (Integer cropTypeId : dto.getCropTypeIds()) {
			CropType cropType = cropTypeRepository.findById(cropTypeId)
					.orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado: " + cropTypeId));

			ActivityCropType activityCropType = new ActivityCropType();
			activityCropType.setActivity(savedActivity);
			activityCropType.setCropType(cropType);
			activityCropType.setCreatedAt(LocalDateTime.now());
			activityCropType.setCreatedBy(updatedBy);

			activityCropTypeRepository.save(activityCropType);
		}

		// 7. Atualizar reward e activity_rewards para manter pontos/validade
		// sincronizados

		List<ActivityReward> rewards = activityRewardRepository.findByActivityId(activityId);
		for (ActivityReward ar : rewards) {

			// Atualizar Reward (tabela rewards)
			Reward reward = ar.getReward();
			reward.setPointsGain(dto.getPoints());
			reward.setValidFrom(dto.getValidFrom());
			reward.setValidTo(dto.getValidTo());
			reward.setUpdatedAt(LocalDateTime.now());
			reward.setUpdatedBy(updatedBy);
			rewardRepository.save(reward);

			// Atualizar ActivityReward (tabela activity_rewards)
			ar.setPointsGain(dto.getPoints());
			ar.setCreatedAt(ar.getCreatedAt() != null ? ar.getCreatedAt() : LocalDateTime.now());
			activityRewardRepository.save(ar);
		}

		// 8. Montar resposta
		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("companyId", company.getId());
		response.put("description", savedActivity.getDescription());
		response.put("points", savedActivity.getPoints());
		response.put("status", savedActivity.getActivityStatus().getCode());
		response.put("validFrom", savedActivity.getValidFrom());
		response.put("validTo", savedActivity.getValidTo());
		response.put("cropTypesCount", dto.getCropTypeIds().size());
		response.put("message", "Atividade atualizada com sucesso!");
		response.put("thumbnailUrl", savedActivity.getThumbnailUrl());
		response.put("thumbnailGsutilUri", savedActivity.getThumbnailGsutilUri());

		return response;
	}

	@Transactional
	public Map<String, Object> updateActivityThumbnail(Integer activityId, MultipartFile thumbnail, Integer userId) throws IOException {

	    Activity activity = activityRepository.findById(activityId)
	            .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

	    // Validar permissão
	    if (!activity.getCreatedBy().equals(userId)) {
	        throw new AccessDeniedException("Sem permissão para atualizar esta atividade");
	    }

	    // Validar arquivo
	    validateThumbnailFile(thumbnail);

	    // Deletar thumbnail anterior se existir
	    if (activity.getThumbnailUrl() != null && !activity.getThumbnailUrl().isEmpty()) {
	        try {
	            fileStorageService.deleteFile(activity.getThumbnailUrl());
	        } catch (Exception e) {
	            System.err.println("Erro ao deletar thumbnail anterior: " + e.getMessage());
	        }
	    }

	    // Upload da nova
	    StoredFileInfo stored = fileStorageService.uploadFile(thumbnail);
	    activity.setThumbnailUrl(stored.getFileUrl());
	    activity.setThumbnailGsutilUri(stored.getGsutilUri());
	    activity.setUpdatedAt(LocalDateTime.now());
	    activity.setUpdatedBy(userId);

	    activityRepository.save(activity);

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("id", activity.getId());
	    response.put("thumbnailUrl", activity.getThumbnailUrl());
	    response.put("thumbnailGsutilUri", activity.getThumbnailGsutilUri());
	    response.put("message", "Thumbnail atualizada com sucesso!");

	    return response;
	}
	
	@Transactional
	public List<ActivityListDTO> listActivities(Integer companyId, String statusCode, Integer cropTypeId,
			Integer farmId, LocalDate startDate, LocalDate endDate) {

		boolean hasStatus = statusCode != null && !statusCode.isBlank();
		boolean hasCropType = cropTypeId != null;
		boolean hasFarm = farmId != null;
		boolean hasStart = startDate != null;
		boolean hasEnd = endDate != null;

		String normalizedStatus = hasStatus ? statusCode.toLowerCase() : null;

		List<Activity> activities;

		if (hasStatus || hasCropType || hasFarm || hasStart || hasEnd) {
			activities = activityRepository.findWithFilters(companyId, normalizedStatus,
					hasCropType ? cropTypeId : null, hasFarm ? farmId : null, hasStart ? startDate : null,
					hasEnd ? endDate : null);
		} else {
			activities = activityRepository.findByCompanyId(companyId);
		}

		return activities.stream().map(this::toActivityListDTO).toList();
	}

	@Transactional
	public ActivityListDTO listActivity(Integer idActivity) {

		Activity activity = activityRepository.findById(idActivity)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		return toActivityListDTO(activity);
	}

	private ActivityListDTO toActivityListDTO(Activity activity) {
		ActivityListDTO dto = new ActivityListDTO();
		dto.setId(activity.getId());
		dto.setCompanyId(activity.getCompany().getId());
		dto.setDescription(activity.getDescription());
		dto.setPoints(activity.getPoints());
		dto.setStatus(activity.getActivityStatus().getCode());
		dto.setValidFrom(activity.getValidFrom());
		dto.setValidTo(activity.getValidTo());
		dto.setName(activity.getName());
		dto.setThumbnailUrl(activity.getThumbnailUrl());
		dto.setThumbnailGsutilUri(activity.getThumbnailGsutilUri());

		// buscar cropTypes dessa activity
		List<Integer> cropTypeIds = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(actCrop -> actCrop.getCropType().getId()).toList();

		dto.setCropTypeIds(cropTypeIds);

		return dto;
	}

	@Transactional
	public void cancelDraftActivity(Integer activityId) {
		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Apenas atividades em draft podem ser canceladas");
		}

		ActivityStatus canceledStatus = activityStatusRepository.findByCode("canceled")
				.orElseThrow(() -> new IllegalArgumentException("Status 'canceled' não configurado"));

		activity.setActivityStatus(canceledStatus);
		activityRepository.save(activity);
	}

	/**
	 * Valida se a empresa possui pelo menos um produtor com farm que tenha cada um
	 * dos cropTypes.
	 */
	private void validateCompanyHasCropTypes(Company company, List<Integer> cropTypeIds) {

		// 1. Buscar todas as farms da empresa
		List<Farm> companyFarms = farmRepository.findByCompanyId(company.getId());

		if (companyFarms.isEmpty()) {
			throw new BusinessException("Empresa não possui nenhuma fazenda registrada");
		}

		// 2. Buscar todos os crops dessas farms
		List<FarmCrop> farmCrops = companyFarms.stream()
				.flatMap(farm -> farmCropRepository.findByFarmId(farm.getId()).stream()).toList();

		// 3. Extrair IDs dos crops que existem
		Set<Integer> existingCropTypeIds = farmCrops.stream().map(fc -> fc.getCropType().getId())
				.collect(Collectors.toSet());

		// 4. Verificar se todos os crops da atividade existem nas farms
		for (Integer cropTypeId : cropTypeIds) {
			if (!existingCropTypeIds.contains(cropTypeId)) {
				CropType cropType = cropTypeRepository.findById(cropTypeId)
						.orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado"));
				throw new BusinessException(
						"Nenhum produtor da empresa possui farm com a cultura: " + cropType.getName());
			}
		}
	}

	// ========== UPLOAD DE THUMBNAIL ==========
	public Map<String, Object> uploadActivityThumbnail(Integer activityId, MultipartFile thumbnail, Integer userId)
	        throws IOException {

	    Activity activity = activityRepository.findById(activityId)
	            .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

	    // Validar permissão (apenas criador da atividade)
	    if (!activity.getCreatedBy().equals(userId)) {
	        throw new AccessDeniedException("Sem permissão para atualizar esta atividade");
	    }

	    // Validar arquivo
	    validateThumbnailFile(thumbnail);

	    // Se já existe thumbnail, deleta
	    if (activity.getThumbnailUrl() != null && !activity.getThumbnailUrl().isEmpty()) {
	        deleteFileFromGcs(activity.getThumbnailUrl());
	    }

	    // Upload da nova thumbnail
	    StoredFileInfo stored = fileStorageService.uploadFile(thumbnail);
	    activity.setThumbnailUrl(stored.getFileUrl());
	    activity.setThumbnailGsutilUri(stored.getGsutilUri());

	    activityRepository.save(activity);

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("id", activity.getId());
	    response.put("thumbnailUrl", activity.getThumbnailUrl());
	    response.put("thumbnailGsutilUri", activity.getThumbnailGsutilUri());
	    response.put("message", "Thumbnail atualizada com sucesso!");

	    return response;
	}

	// ========== HELPERS ==========

	private void validateThumbnailFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("Arquivo vazio não é permitido");
		}

		if (file.getSize() > MAX_THUMBNAIL_SIZE) {
			throw new BusinessException("Arquivo não pode exceder 5MB");
		}

		String contentType = file.getContentType();
		if (contentType == null || !isThumbnailContentType(contentType)) {
			throw new BusinessException("Tipo de arquivo inválido. Aceitos: PNG, JPEG, JPG");
		}
	}

	private boolean isThumbnailContentType(String contentType) {
		return contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg");
	}

	private void deleteFileFromGcs(String fileUrl) {
	    try {
	        fileStorageService.deleteFile(fileUrl);
	        System.out.println("Thumbnail anterior deletada com sucesso");
	    } catch (Exception e) {
	        System.err.println("Erro ao deletar thumbnail anterior: " + e.getMessage());
	    }
	}

}