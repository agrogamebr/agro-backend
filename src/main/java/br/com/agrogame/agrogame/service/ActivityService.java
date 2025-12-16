package br.com.agrogame.agrogame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.ActivityListDTO;
import br.com.agrogame.agrogame.dto.CreateActivityDTO;
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

	public ActivityService(ActivityRepository activityRepository, CompanyRepository companyRepository,
			ActivityStatusRepository activityStatusRepository, ActivityRewardRepository activityRewardRepository,
			RewardRepository rewardRepository, RewardStatusRepository rewardStatusRepository,
			CropTypeRepository cropTypeRepository, ActivityCropTypeRepository activityCropTypeRepository,
			UserActivityRepository userActivityRepository, UserActivityStatusRepository userActivityStatusRepository,
			FarmRepository farmRepository, FarmCropRepository farmCropRepository, UserRepository userRepository) {
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
	}

	@Transactional
	public Map<String, Object> registerActivity(CreateActivityDTO dto, Integer createdBy) {

	    Company company = companyRepository.findById(dto.getCompanyId())
	            .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

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
	 * Cria UserActivity para todas as farms da empresa que possuem um dos cropTypes da atividade.
	 */
	private void createUserActivitiesForMatchingFarms(Activity activity, Integer createdBy) {
	    
	    UserActivityStatus pendingStatus = userActivityStatusRepository.findByCode("pending")
	            .orElseThrow(() -> new BusinessException("Status 'pending' não configurado"));
	    
		User userCreatedBy = userRepository.findById(createdBy)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

	    Company company = activity.getCompany();

	    // 1. Buscar todos os cropTypeIds da atividade
	    List<Integer> activityCropTypeIds = activityCropTypeRepository.findByActivityId(activity.getId())
	            .stream()
	            .map(act -> act.getCropType().getId())
	            .toList();

	    // 2. Buscar todas as farms da empresa
	    List<Farm> companyFarms = farmRepository.findByCompanyId(company.getId());

	    // 3. Para cada farm, verificar se tem algum dos crops da atividade
	    for (Farm farm : companyFarms) {
	        List<FarmCrop> farmCrops = farmCropRepository.findByFarmId(farm.getId());

	        boolean hasCrop = farmCrops.stream()
	                .anyMatch(fc -> activityCropTypeIds.contains(fc.getCropType().getId()));

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


	private void notifyProducers(Activity activity) {
		// TODO: Implementar lógica de notificação
		// Buscar producers da empresa com as culturas selecionadas
		// Criar registros em user_activities
		// Enviar notificação (email, push, etc)
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
	 * Valida se a empresa possui pelo menos um produtor com farm que tenha cada um dos cropTypes.
	 */
	private void validateCompanyHasCropTypes(Company company, List<Integer> cropTypeIds) {
	    
	    // 1. Buscar todas as farms da empresa
	    List<Farm> companyFarms = farmRepository.findByCompanyId(company.getId());

	    if (companyFarms.isEmpty()) {
	        throw new BusinessException("Empresa não possui nenhuma fazenda registrada");
	    }

	    // 2. Buscar todos os crops dessas farms
	    List<FarmCrop> farmCrops = companyFarms.stream()
	            .flatMap(farm -> farmCropRepository.findByFarmId(farm.getId()).stream())
	            .toList();

	    // 3. Extrair IDs dos crops que existem
	    Set<Integer> existingCropTypeIds = farmCrops.stream()
	            .map(fc -> fc.getCropType().getId())
	            .collect(Collectors.toSet());

	    // 4. Verificar se todos os crops da atividade existem nas farms
	    for (Integer cropTypeId : cropTypeIds) {
	        if (!existingCropTypeIds.contains(cropTypeId)) {
	            CropType cropType = cropTypeRepository.findById(cropTypeId)
	                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado"));
	            throw new BusinessException(
	                "Nenhum produtor da empresa possui farm com a cultura: " + cropType.getName()
	            );
	        }
	    }
	}
}