package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import br.com.agrogame.agrogame.model.ActivityDraft;
import br.com.agrogame.agrogame.model.ActivityReward;
import br.com.agrogame.agrogame.model.ActivityStatus;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.FarmCrop;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.Reward;
import br.com.agrogame.agrogame.model.RewardStatus;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
import br.com.agrogame.agrogame.repository.ActivityDraftRepository;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.ActivityRewardRepository;
import br.com.agrogame.agrogame.repository.ActivityStatusRepository;
import br.com.agrogame.agrogame.repository.CompanyRepository;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.FarmCropRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
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
	private final ProductionUnitRepository productionUnitRepository;
	private final ActivityDraftRepository activityDraftRepository;

	private static final long MAX_THUMBNAIL_SIZE = 5 * 1024 * 1024; // 5MB

	public ActivityService(ActivityRepository activityRepository, CompanyRepository companyRepository,
			ActivityStatusRepository activityStatusRepository, ActivityRewardRepository activityRewardRepository,
			RewardRepository rewardRepository, RewardStatusRepository rewardStatusRepository,
			CropTypeRepository cropTypeRepository, ActivityCropTypeRepository activityCropTypeRepository,
			UserActivityRepository userActivityRepository, UserActivityStatusRepository userActivityStatusRepository,
			FarmRepository farmRepository, FarmCropRepository farmCropRepository, UserRepository userRepository,
			FileStorageService fileStorageService, ProductionUnitRepository productionUnitRepository,
			ActivityDraftRepository activityDraftRepository) {
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
		this.productionUnitRepository = productionUnitRepository;
		this.activityDraftRepository = activityDraftRepository;
	}

	@Transactional
	public Map<String, Object> registerActivity(CreateActivityMultipartDTO dto, Company company, Integer createdBy)
			throws IOException {

		boolean isSendNow = dto.getSendNow();
		String statusCode = isSendNow ? "send" : "draft";

		ActivityStatus status = activityStatusRepository.findByCode(statusCode)
				.orElseThrow(() -> new BusinessException("Status '" + statusCode + "' não configurado"));

		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new BusinessException("Data inicial não pode ser maior que a data final");
		}

		List<Integer> cropTypeIds = dto.getCropTypeIds();
		boolean hasCrops = cropTypeIds != null && !cropTypeIds.isEmpty();
		boolean hasFarms = dto.getFarmIds() != null && !dto.getFarmIds().isEmpty();

		if (!hasCrops && !hasFarms) {
			throw new BusinessException("Informe pelo menos um tipo de cultura ou uma fazenda.");
		}

		// ========== VALIDAÇÕES GERAIS (Empresa, Crops) ==========
		if (hasCrops) {
			validateCompanyHasCropTypes(company, cropTypeIds);
		}

		// ========== CRIAR/SALVAR ACTIVITY ==========
		Activity activity = new Activity();
		activity.setCompany(company);
		activity.setDescription(dto.getDescription());
		activity.setName(dto.getName());
		activity.setPoints(dto.getPoints());
		activity.setActivityStatus(status);
		activity.setValidFrom(dto.getValidFrom());
		activity.setValidTo(dto.getValidTo());
		activity.setCreatedAt(LocalDateTime.now());
		activity.setCreatedBy(createdBy);

		if (isSendNow) {
			activity.setUpdatedAt(LocalDateTime.now());
			activity.setUpdatedBy(createdBy);
		}

		if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
			validateThumbnailFile(dto.getThumbnail());
			StoredFileInfo stored = fileStorageService.uploadFile(dto.getThumbnail());
			activity.setThumbnailUrl(stored.getFileUrl());
			activity.setThumbnailGsutilUri(stored.getGsutilUri());
		}

		Activity savedActivity = activityRepository.save(activity);

		// ========== VINCULAR CROP TYPES ==========
		if (hasCrops) {
			for (Integer cropTypeId : cropTypeIds) {
				CropType cropType = cropTypeRepository.findById(cropTypeId)
						.orElseThrow(() -> new ResourceNotFoundException("CropType " + cropTypeId + " não encontrado"));

				ActivityCropType act = new ActivityCropType();
				act.setActivity(savedActivity);
				act.setCropType(cropType);
				act.setCreatedAt(LocalDateTime.now());
				act.setCreatedBy(createdBy);
				activityCropTypeRepository.save(act);
			}
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

		// ========== LÓGICA DE DRAFT vs SEND ==========

		if (!isSendNow) {
			saveDraftSelections(savedActivity, dto, createdBy);
		} else {
			List<Farm> targetFarms;
			if (dto.getFarmIds() != null && !dto.getFarmIds().isEmpty()) {
				targetFarms = farmRepository.findAllById(dto.getFarmIds());
			} else if (hasCrops) {
				targetFarms = farmRepository.findByCompanyIdAndCropTypes(company.getId(), cropTypeIds);
			} else {
				throw new BusinessException("Nenhuma fazenda encontrada para os critérios selecionados.");
			}

			if (targetFarms.isEmpty()) {
				throw new BusinessException("Nenhuma fazenda encontrada para os critérios selecionados.");
			}

			createUserActivitiesForMatchingFarms(savedActivity, createdBy, targetFarms, dto.getProductionUnitIds());
			activityDraftRepository.findByActivityId(savedActivity.getId()).ifPresent(activityDraftRepository::delete);
		}

		// ========== RESPOSTA ==========
		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("status", status.getCode());
		response.put("message", isSendNow ? "Atividade criada e enviada com sucesso!"
				: "Atividade criada como 'draft'. Valide e envie posteriormente.");
		response.put("thumbnailUrl", savedActivity.getThumbnailUrl());
		response.put("thumbnailGsutilUri", savedActivity.getThumbnailGsutilUri());
		return response;
	}

	/**
	 * Método auxiliar para salvar o rascunho usando as novas colunas JSONB
	 */
	private void saveDraftSelections(Activity activity, CreateActivityMultipartDTO dto, Integer userId) {
		// Busca ou cria novo (Upsert)
		ActivityDraft draft = activityDraftRepository.findByActivityId(activity.getId()).orElse(new ActivityDraft());

		// Se for novo, vincula dados base
		if (draft.getId() == null) {
			draft.setActivity(activity);
			draft.setUserId(userId);
			draft.setCreatedAt(LocalDateTime.now());
			draft.setCreatedBy(userId);
		}

		// Auditoria de update (Evita erro de not-null)
		draft.setUpdatedAt(LocalDateTime.now());
		draft.setUpdatedBy(userId);

		// Preenche as listas JSONB (Hibernate converte automaticamente)
		draft.setFarmIds(dto.getFarmIds() != null ? dto.getFarmIds() : new ArrayList<>());
		draft.setCropTypeIds(dto.getCropTypeIds() != null ? dto.getCropTypeIds() : new ArrayList<>());
		draft.setProductionUnitIds(dto.getProductionUnitIds() != null ? dto.getProductionUnitIds() : new ArrayList<>());

		activityDraftRepository.save(draft);
	}

	@Transactional
	public Map<String, Object> sendActivity(Integer activityId, Integer sentBy) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Apenas atividades em draft podem ser enviadas");
		}

		// 1. Atualizar status para SEND
		ActivityStatus sendStatus = activityStatusRepository.findByCode("send")
				.orElseThrow(() -> new BusinessException("Status 'send' não configurado"));

		activity.setActivityStatus(sendStatus);
		activity.setUpdatedAt(LocalDateTime.now());
		activity.setUpdatedBy(sentBy);
		Activity savedActivity = activityRepository.save(activity);

		// 2. Recuperar as escolhas do Draft (JSONB)
		Optional<ActivityDraft> draftOpt = activityDraftRepository.findByActivityId(activityId);

		List<Farm> targetFarms = new ArrayList<>();
		List<Integer> targetUnitIds = null;

		if (draftOpt.isEmpty()) {
			// Fallback: Se não tem draft, usa lógica padrão (Todas as farms dos crops
			// vinculados à atividade)
			List<Integer> cropIds = activityCropTypeRepository.findByActivityId(activityId).stream()
					.map(act -> act.getCropType().getId()).toList();
			targetFarms = farmRepository.findByCompanyIdAndCropTypes(activity.getCompany().getId(), cropIds);
		} else {
			ActivityDraft draft = draftOpt.get();

			List<Integer> farmIds = draft.getFarmIds();
			List<Integer> unitIds = draft.getProductionUnitIds();
			List<Integer> cropIds = draft.getCropTypeIds();

			// Lógica de Farms: Se tem IDs salvos, usa eles. Se não, busca pelos Crops
			// salvos.
			if (farmIds != null && !farmIds.isEmpty()) {
				targetFarms = farmRepository.findAllById(farmIds);
			} else if (cropIds != null && !cropIds.isEmpty()) {
				targetFarms = farmRepository.findByCompanyIdAndCropTypes(activity.getCompany().getId(), cropIds);
			}

			// Lógica de UPs
			if (unitIds != null && !unitIds.isEmpty()) {
				targetUnitIds = new ArrayList<>(unitIds);
			}
		}

		if (targetFarms.isEmpty()) {
			throw new BusinessException("Não há fazendas elegíveis para envio com os critérios salvos no rascunho.");
		}

		// 3. Executar Fan-Out
		createUserActivitiesForMatchingFarms(savedActivity, sentBy, targetFarms, targetUnitIds);

		// 4. Limpar tabela de Draft
		draftOpt.ifPresent(activityDraftRepository::delete);

		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("status", "send");
		response.put("message", "Atividade enviada e rascunho limpo!");
		return response;
	}

	/**
	 * Cria UserActivity para farms/units compatíveis.
	 */
	private void createUserActivitiesForMatchingFarms(Activity activity, Integer createdBy, List<Farm> targetFarms,
			List<Integer> productionUnitIds) {

		UserActivityStatus pendingStatus = userActivityStatusRepository.findByCode("pending")
				.orElseThrow(() -> new BusinessException("Status 'pending' não configurado"));

		User userCreatedBy = userRepository.findById(createdBy)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Company company = activity.getCompany();

		List<Integer> activityCropTypeIds = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(act -> act.getCropType().getId()).toList();

		boolean hasActivityCrops = !activityCropTypeIds.isEmpty();
		boolean hasSpecificUnits = productionUnitIds != null && !productionUnitIds.isEmpty();

// 1) Carrega UserActivity já existentes para essa Activity
		List<UserActivity> existing = userActivityRepository.findByActivityId(activity.getId());

// 2) Monta um Set com chaves (userId, farmId, unitId) já criadas
		record UAKey(Integer userId, Integer farmId, Integer unitId) {
		}

		Set<UAKey> existingKeys = existing.stream()
				.map(ua -> new UAKey(ua.getUser().getId(), ua.getFarm().getId(),
						ua.getProductionUnit() != null ? ua.getProductionUnit().getId() : null))
				.collect(Collectors.toSet());

		List<Farm> farmsToProcess;
		if (targetFarms != null && !targetFarms.isEmpty()) {
			farmsToProcess = targetFarms;
		} else {
			farmsToProcess = farmRepository.findByCompanyIdAndIsActiveTrue(company.getId());
		}

		for (Farm farm : farmsToProcess) {
			if (hasActivityCrops) {
				List<FarmCrop> farmCrops = farmCropRepository.findByFarmId(farm.getId());
				boolean hasCrop = farmCrops.stream()
						.anyMatch(fc -> activityCropTypeIds.contains(fc.getCropType().getId()));

				if (!hasCrop) {
					continue;
				}
			}

			if (hasSpecificUnits) {
// Units desta farm que estão na lista e são compatíveis
				List<ProductionUnit> unitsForThisFarm = productionUnitRepository
						.findByIdInAndFarmIdAndCropTypesCompatible(productionUnitIds, farm.getId(),
								activityCropTypeIds);

				if (unitsForThisFarm.isEmpty()) {
					continue;
				}

				for (ProductionUnit pu : unitsForThisFarm) {
					UAKey key = new UAKey(farm.getOwner().getId(), farm.getId(), pu.getId());
					if (existingKeys.contains(key)) {
						continue; // já existe, não cria de novo
					}

					UserActivity ua = new UserActivity();
					ua.setActivity(activity);
					ua.setUser(farm.getOwner());
					ua.setFarm(farm);
					ua.setProductionUnit(pu);
					ua.setStatus(pendingStatus);
					ua.setCreatedAt(LocalDateTime.now());
					ua.setCreatedBy(userCreatedBy);
					userActivityRepository.save(ua);

					existingKeys.add(key);
				}

			} else {
// Sem UP específica (comportamento padrão)
				List<ProductionUnit> units = productionUnitRepository.findByFarmAndCropTypesCompatible(farm.getId(),
						activityCropTypeIds);

				if (units.isEmpty()) {
					UAKey key = new UAKey(farm.getOwner().getId(), farm.getId(), null);
					if (existingKeys.contains(key)) {
						continue;
					}

					UserActivity ua = new UserActivity();
					ua.setActivity(activity);
					ua.setUser(farm.getOwner());
					ua.setFarm(farm);
					ua.setStatus(pendingStatus);
					ua.setCreatedAt(LocalDateTime.now());
					ua.setCreatedBy(userCreatedBy);
					userActivityRepository.save(ua);

					existingKeys.add(key);
				} else {
					for (ProductionUnit pu : units) {
						UAKey key = new UAKey(farm.getOwner().getId(), farm.getId(), pu.getId());
						if (existingKeys.contains(key)) {
							continue;
						}

						UserActivity ua = new UserActivity();
						ua.setActivity(activity);
						ua.setUser(farm.getOwner());
						ua.setFarm(farm);
						ua.setProductionUnit(pu);
						ua.setStatus(pendingStatus);
						ua.setCreatedAt(LocalDateTime.now());
						ua.setCreatedBy(userCreatedBy);
						userActivityRepository.save(ua);

						existingKeys.add(key);
					}
				}
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

		// 3. Pegar empresa a partir da Activity (e opcionalmente validar com o usuário
		// logado)
		Company company = activity.getCompany();
		if (company == null) {
			throw new BusinessException("Atividade não está vinculada a nenhuma empresa");
		}

		// (opcional, se quiser garantir que o usuário logado é da mesma empresa)
		User updatedUser = userRepository.findById(updatedBy)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
		if (updatedUser.getCompany() == null || !updatedUser.getCompany().getId().equals(company.getId())) {
			throw new AccessDeniedException("Usuário não pertence à empresa da atividade");
		}

		// 4. Validar datas
		if (dto.getValidFrom().isAfter(dto.getValidTo())) {
			throw new BusinessException("Data inicial não pode ser maior que a data final");
		}

		List<Integer> cropTypeIds = dto.getCropTypeIds();
		boolean hasCrops = cropTypeIds != null && !cropTypeIds.isEmpty();
		boolean hasFarms = dto.getFarmIds() != null && !dto.getFarmIds().isEmpty();

		if (!hasCrops && !hasFarms) {
			throw new BusinessException("Informe pelo menos um tipo de cultura ou uma fazenda.");
		}

		if (hasCrops) {
			validateCompanyHasCropTypes(company, cropTypeIds);
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

		if (hasCrops) {
			for (Integer cropTypeId : cropTypeIds) {
				CropType cropType = cropTypeRepository.findById(cropTypeId).orElseThrow(
						() -> new ResourceNotFoundException("Tipo de cultura não encontrado: " + cropTypeId));

				ActivityCropType activityCropType = new ActivityCropType();
				activityCropType.setActivity(savedActivity);
				activityCropType.setCropType(cropType);
				activityCropType.setCreatedAt(LocalDateTime.now());
				activityCropType.setCreatedBy(updatedBy);

				activityCropTypeRepository.save(activityCropType);
			}
		}

		// 7. Atualizar rewards
		List<ActivityReward> rewards = activityRewardRepository.findByActivityId(activityId);
		for (ActivityReward ar : rewards) {
			Reward reward = ar.getReward();
			reward.setPointsGain(dto.getPoints());
			reward.setValidFrom(dto.getValidFrom());
			reward.setValidTo(dto.getValidTo());
			reward.setUpdatedAt(LocalDateTime.now());
			reward.setUpdatedBy(updatedBy);
			rewardRepository.save(reward);

			ar.setPointsGain(dto.getPoints());
			ar.setCreatedAt(ar.getCreatedAt() != null ? ar.getCreatedAt() : LocalDateTime.now());
			activityRewardRepository.save(ar);
		}

		ActivityDraft draft = activityDraftRepository.findByActivityId(activityId).orElse(new ActivityDraft());

		// Dados Básicos
		draft.setActivity(savedActivity);
		draft.setUserId(updatedBy); // Vincula ao usuário que editou

		// Auditoria
		if (draft.getId() == null) {
			draft.setCreatedAt(LocalDateTime.now());
			draft.setCreatedBy(updatedBy);
		}
		draft.setUpdatedAt(LocalDateTime.now());
		draft.setUpdatedBy(updatedBy);

		// DADOS DAS LISTAS (Mágica do JSONB acontece aqui)
		// Apenas passamos as listas do DTO para a entidade
		draft.setFarmIds(dto.getFarmIds() != null ? dto.getFarmIds() : new ArrayList<>());
		draft.setProductionUnitIds(dto.getProductionUnitIds() != null ? dto.getProductionUnitIds() : new ArrayList<>());
		draft.setCropTypeIds(dto.getCropTypeIds() != null ? dto.getCropTypeIds() : new ArrayList<>());

		// Salva (Insert ou Update automático)
		activityDraftRepository.save(draft);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Atividade atualizada com sucesso!");

		// Dados Principais da Atividade
		response.put("id", savedActivity.getId());
		response.put("name", savedActivity.getName());
		response.put("description", savedActivity.getDescription());
		response.put("points", savedActivity.getPoints());
		response.put("status", savedActivity.getActivityStatus().getCode()); // "draft"
		response.put("validFrom", savedActivity.getValidFrom());
		response.put("validTo", savedActivity.getValidTo());
		response.put("thumbnailUrl", savedActivity.getThumbnailUrl());
		response.put("thumbnailGsutilUri", savedActivity.getThumbnailGsutilUri());

		// Dados das Listas (Draft)
		response.put("farmIds", dto.getFarmIds());
		response.put("productionUnitIds", dto.getProductionUnitIds());
		response.put("cropTypeIds", dto.getCropTypeIds());

		return response;
	}

	@Transactional
	public Map<String, Object> updateActivityThumbnail(Integer activityId, MultipartFile thumbnail, Integer userId)
			throws IOException {

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
	public Page<ActivityListDTO> listActivities(Integer companyId, String statusCode, List<Integer> cropTypeIds, // List
			List<Integer> farmIds, List<Integer> productionUnitIds, LocalDate startDate, LocalDate endDate, int page,
			int size) {

		String normalizedStatus = (statusCode != null && !statusCode.isBlank()) ? statusCode.toLowerCase() : null;

		List<Integer> crops = (cropTypeIds != null && !cropTypeIds.isEmpty()) ? cropTypeIds : null;
		List<Integer> farms = (farmIds != null && !farmIds.isEmpty()) ? farmIds : null;
		List<Integer> units = (productionUnitIds != null && !productionUnitIds.isEmpty()) ? productionUnitIds : null;

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "validFrom"));

		Page<Activity> activitiesPage = activityRepository.findWithFilters(companyId, normalizedStatus, crops, farms,
				units, startDate, endDate, pageable);

		return activitiesPage.map(this::toActivityListDTO);
	}

	@Transactional
	public ActivityListDTO listActivity(Integer idActivity) {

		// 1. Busca a Atividade Oficial
		Activity activity = activityRepository.findById(idActivity)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		// 2. Converte para DTO (Dados Básicos)
		ActivityListDTO dto = toActivityListDTO(activity);

		// 3. SE FOR DRAFT: Busca e mescla os dados do Rascunho
		if ("draft".equalsIgnoreCase(activity.getActivityStatus().getCode())) {
			enrichDtoWithDraftData(dto, idActivity);
		} else {
			List<UserActivity> uas = userActivityRepository.findByActivityId(activity.getId());

			List<Integer> userActivityIds = uas.stream().map(UserActivity::getId).toList();

			List<Integer> farmIds = uas.stream().map(ua -> ua.getFarm().getId()).distinct().toList();

			List<Integer> unitIds = uas.stream().map(UserActivity::getProductionUnit).filter(Objects::nonNull)
					.map(pu -> pu.getId()).distinct().toList();

			dto.setUserActivityIds(userActivityIds);
			dto.setFarmIds(farmIds);
			dto.setProductionUnitIds(unitIds);
		}
		return dto;
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

		// novos campos
		dto.setCreatedAt(activity.getCreatedAt());
		dto.setCreatedBy(activity.getCreatedBy());
		dto.setUpdatedAt(activity.getUpdatedAt());
		dto.setUpdatedBy(activity.getUpdatedBy());

		List<Integer> officialCrops = activityCropTypeRepository.findByActivityId(activity.getId()).stream()
				.map(act -> act.getCropType().getId()).toList();
		dto.setCropTypeIds(officialCrops);

		return dto;
	}

	// Método auxiliar para buscar dados do Draft (JSONB)
	private void enrichDtoWithDraftData(ActivityListDTO dto, Integer activityId) {
		Optional<ActivityDraft> draftOpt = activityDraftRepository.findByActivityId(activityId);

		if (draftOpt.isPresent()) {
			ActivityDraft draft = draftOpt.get();

			if (draft.getFarmIds() != null && !draft.getFarmIds().isEmpty()) {
				dto.setFarmIds(draft.getFarmIds());
			} else {
				dto.setFarmIds(new ArrayList<>());
			}

			if (draft.getProductionUnitIds() != null && !draft.getProductionUnitIds().isEmpty()) {
				dto.setProductionUnitIds(draft.getProductionUnitIds());
			} else {
				dto.setProductionUnitIds(new ArrayList<>());
			}

			if (draft.getCropTypeIds() != null && !draft.getCropTypeIds().isEmpty()) {
				dto.setCropTypeIds(draft.getCropTypeIds());
			}
		} else {
			List<UserActivity> uas = userActivityRepository.findByActivityId(activityId);
			List<Integer> farmIds = uas.stream().map(ua -> ua.getFarm().getId()).distinct().toList();

			List<Integer> unitIds = uas.stream().map(ua -> ua.getProductionUnit()).filter(Objects::nonNull)
					.map(ProductionUnit::getId).distinct().toList();

			dto.setFarmIds(farmIds);
			dto.setProductionUnitIds(unitIds);
		}
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