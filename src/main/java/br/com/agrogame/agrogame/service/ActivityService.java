package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.ActivityListDTO;
import br.com.agrogame.agrogame.dto.CreateActivityDTO;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityCropType;
import br.com.agrogame.agrogame.model.ActivityReward;
import br.com.agrogame.agrogame.model.ActivityStatus;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Reward;
import br.com.agrogame.agrogame.model.RewardStatus;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.ActivityRewardRepository;
import br.com.agrogame.agrogame.repository.ActivityStatusRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.RewardRepository;
import br.com.agrogame.agrogame.repository.RewardStatusRepository;
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

	public ActivityService(ActivityRepository activityRepository, CompanyRepository companyRepository,
			ActivityStatusRepository activityStatusRepository, ActivityRewardRepository activityRewardRepository,
			RewardRepository rewardRepository, RewardStatusRepository rewardStatusRepository,
			CropTypeRepository cropTypeRepository, ActivityCropTypeRepository activityCropTypeRepository) {
		this.activityRepository = activityRepository;
		this.companyRepository = companyRepository;
		this.activityStatusRepository = activityStatusRepository;
		this.activityRewardRepository = activityRewardRepository;
		this.rewardRepository = rewardRepository;
		this.rewardStatusRepository = rewardStatusRepository;
		this.cropTypeRepository = cropTypeRepository;
		this.activityCropTypeRepository = activityCropTypeRepository;
	}

	@Transactional
	public Map<String, Object> registerActivity(CreateActivityDTO dto, Integer createdBy) {

		Company company = companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

		ActivityStatus draftStatus = activityStatusRepository.findByCode("draft")
				.orElseThrow(() -> new IllegalArgumentException("Status 'draft' não configurado"));

		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new IllegalArgumentException("Data inicial não pode ser maior que a data final");
		}

		// ========== CRIAR ACTIVITY ==========
		Activity activity = new Activity();
		activity.setCompany(company);
		activity.setDescription(dto.getDescription());
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
					.orElseThrow(() -> new IllegalArgumentException("Tipo de cultura não encontrado: " + cropTypeId));

			ActivityCropType activityCropType = new ActivityCropType();
			activityCropType.setActivity(savedActivity);
			activityCropType.setCropType(cropType);
			activityCropType.setCreatedAt(LocalDateTime.now());
			activityCropType.setCreatedBy(createdBy);

			activityCropTypeRepository.save(activityCropType);
		}

		// ========== CRIAR REWARD ==========
		RewardStatus rewardStatus = rewardStatusRepository.findByCode("active")
				.orElseThrow(() -> new IllegalArgumentException("Status de recompensa 'active' não configurado"));

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
		response.put("companyId", company.getId());
		response.put("description", savedActivity.getDescription());
		response.put("points", savedActivity.getPoints());
		response.put("status", draftStatus.getCode());
		response.put("validFrom", savedActivity.getValidFrom());
		response.put("validTo", savedActivity.getValidTo());
		response.put("cropTypesCount", dto.getCropTypeIds().size());
		response.put("rewardId", savedReward.getId());
		response.put("message", "Atividade, tipos de cultura e recompensa cadastrados em status 'draft'.");

		return response;
	}

	@Transactional
	public Map<String, Object> sendActivity(Integer activityId, Integer sentBy) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new IllegalArgumentException("Apenas atividades em draft podem ser enviadas");
		}

		// Mudar status para "send"
		ActivityStatus sendStatus = activityStatusRepository.findByCode("send")
				.orElseThrow(() -> new IllegalArgumentException("Status 'send' não configurado"));

		activity.setActivityStatus(sendStatus);
		activity.setUpdatedAt(LocalDateTime.now());
		activity.setUpdatedBy(sentBy);

		Activity savedActivity = activityRepository.save(activity);

		// AQUI: Notificar producers com a cultura selecionada
		notifyProducers(savedActivity);

		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("status", sendStatus.getCode());
		response.put("message", "Atividade enviada para os producers!");

		return response;
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
				.orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));

		// 2. Validar status = draft
		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new IllegalArgumentException("Atividade não pode ser editada pois não está em status 'draft'");
		}

		// 3. Validar empresa
		Company company = companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

		// 4. Validar datas
		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new IllegalArgumentException("Data inicial não pode ser maior que a data final");
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
					.orElseThrow(() -> new IllegalArgumentException("Tipo de cultura não encontrado: " + cropTypeId));

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
	public List<ActivityListDTO> listActivities(Integer companyId, String statusCode) {

		List<Activity> activities;

		if (statusCode != null && !statusCode.isBlank()) {
			activities = activityRepository.findByCompanyIdAndActivityStatus_Code(companyId, statusCode);
		} else {
			activities = activityRepository.findByCompanyId(companyId);
		}

		return activities.stream().map(this::toActivityListDTO).toList();
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

		// buscar cropTypes dessa activity
		List<Integer> cropTypeIds = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(actCrop -> actCrop.getCropType().getId()).toList();

		dto.setCropTypeIds(cropTypeIds);

		return dto;
	}
}
