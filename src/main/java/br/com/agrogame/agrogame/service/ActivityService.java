package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		// ========== VALIDAÇÕES GERAIS (Empresa, Crops) ==========
		validateCompanyHasCropTypes(company, dto.getCropTypeIds());

		// ========== RESOLVER QUAIS FARMS SERÃO O ALVO ==========
		List<Farm> targetFarms;
		boolean farmsExplicitlySelected = (dto.getFarmIds() != null && !dto.getFarmIds().isEmpty());

		if (!farmsExplicitlySelected) {
			// Caso Genérico: Todas as farms da empresa que tenham esses crops
			targetFarms = farmRepository.findByCompanyIdAndCropTypes(company.getId(), dto.getCropTypeIds());
			if (targetFarms.isEmpty()) {
				throw new BusinessException("Nenhuma fazenda da empresa possui as culturas selecionadas.");
			}
		} else {
			// Caso Específico: Apenas as farms selecionadas
			targetFarms = farmRepository.findByIdInAndCompanyIdAndIsActiveTrue(dto.getFarmIds(), company.getId());
			if (targetFarms.size() != dto.getFarmIds().size()) {
				throw new BusinessException("Uma ou mais fazendas não pertencem à empresa ou estão inativas.");
			}
		}

		// ========== VALIDAÇÃO DE PRODUCTION UNITS (SE HOUVER) ==========
		List<Integer> productionUnitIds = dto.getProductionUnitIds();
		if (productionUnitIds != null && !productionUnitIds.isEmpty()) {
			validateProductionUnitsBelongToFarmsAndCrops(productionUnitIds,
					targetFarms.stream().map(Farm::getId).toList(), dto.getCropTypeIds(), company.getId());
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
		for (Integer cropTypeId : dto.getCropTypeIds()) {
			CropType cropType = cropTypeRepository.findById(cropTypeId)
					.orElseThrow(() -> new ResourceNotFoundException("CropType " + cropTypeId + " não encontrado"));

			ActivityCropType act = new ActivityCropType();
			act.setActivity(savedActivity);
			act.setCropType(cropType);
			act.setCreatedAt(LocalDateTime.now());
			act.setCreatedBy(createdBy);
			activityCropTypeRepository.save(act);
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
			saveDraftSelections(savedActivity, targetFarms, productionUnitIds, dto.getCropTypeIds(), createdBy);
		} else {
			// SE É ENVIO IMEDIATO: Fan-out direto
			createUserActivitiesForMatchingFarms(savedActivity, createdBy, targetFarms, productionUnitIds);

			// (Opcional) Limpa drafts antigos se existirem
			activityDraftRepository.deleteByActivityId(savedActivity.getId());
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

	@Transactional
	public Map<String, Object> sendActivity(Integer activityId, Integer sentBy) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Apenas atividades em draft podem ser enviadas");
		}

		// 1. Atualizar status
		ActivityStatus sendStatus = activityStatusRepository.findByCode("send")
				.orElseThrow(() -> new BusinessException("Status 'send' não configurado"));

		activity.setActivityStatus(sendStatus);
		activity.setUpdatedAt(LocalDateTime.now());
		activity.setUpdatedBy(sentBy);
		Activity savedActivity = activityRepository.save(activity);

		// 2. Recuperar as escolhas do Draft
		List<ActivityDraft> drafts = activityDraftRepository.findByActivityId(activityId);

		List<Farm> targetFarms;
		List<Integer> targetUnitIds;

		if (drafts.isEmpty()) {
			// Fallback: Se não tem draft salvo, recalculamos o padrão "genérico"
			List<Integer> cropTypeIds = activityCropTypeRepository.findByActivityId(activityId).stream()
					.map(act -> act.getCropType().getId()).toList();
			targetFarms = farmRepository.findByCompanyIdAndCropTypes(activity.getCompany().getId(), cropTypeIds);
			targetUnitIds = null;
		} else {
			// Reconstrói as listas a partir do draft
			targetFarms = drafts.stream().map(ActivityDraft::getFarm).distinct().toList();

			targetUnitIds = drafts.stream().map(ActivityDraft::getProductionUnit).filter(java.util.Objects::nonNull)
					.map(ProductionUnit::getId).distinct().toList();

			if (targetUnitIds.isEmpty()) {
				targetUnitIds = null;
			}
		}

		// 3. Executar Fan-Out
		createUserActivitiesForMatchingFarms(savedActivity, sentBy, targetFarms, targetUnitIds);

		// 4. Limpar tabela de Draft
		activityDraftRepository.deleteByActivityId(activityId);

		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("status", "send");
		response.put("message", "Atividade enviada e rascunho limpo!");
		return response;
	}

	/**
	 * Salva as escolhas de Farm/UP/Crop na tabela de rascunho. Cria uma linha para
	 * cada combinação (Cartesiano: Farm x UP x Crop).
	 */
	private void saveDraftSelections(Activity activity, List<Farm> farms, List<Integer> productionUnitIds,
			List<Integer> cropTypeIds, // <--- NOVO PARÂMETRO
			Integer userId) {

		// Limpa rascunho anterior
		activityDraftRepository.deleteByActivityId(activity.getId());

		boolean hasSpecificUnits = (productionUnitIds != null && !productionUnitIds.isEmpty());

		// Carregar os objetos CropType para poder salvar (evita query dentro do loop se
		// possível)
		List<CropType> selectedCrops = cropTypeRepository.findAllById(cropTypeIds);

		for (Farm farm : farms) {

			// Descobre quais UPs salvar para essa fazenda
			List<ProductionUnit> targetUnitsForFarm;

			if (hasSpecificUnits) {
				// Se tem UPs selecionadas, filtra as que são desta fazenda
				List<ProductionUnit> specificUnits = productionUnitRepository.findByIdInAndFarmId(productionUnitIds,
						farm.getId());

				if (specificUnits.isEmpty()) {
					// Selecionou UPs, mas nenhuma é desta fazenda.
					// Regra: Salva a fazenda "genérica" (sem UP específica) ou pula?
					// Vamos assumir "genérica" (null) para garantir que a fazenda receba.
					targetUnitsForFarm = java.util.Collections.singletonList(null);
				} else {
					targetUnitsForFarm = specificUnits;
				}
			} else {
				// Nenhuma UP selecionada -> Salva "null" (representando toda a fazenda)
				targetUnitsForFarm = java.util.Collections.singletonList(null);
			}

			// AGORA O LOOP DE CROPS (Multiplica as linhas)
			for (CropType crop : selectedCrops) {
				for (ProductionUnit pu : targetUnitsForFarm) {
					createAndSaveDraft(activity, farm, pu, crop, userId);
				}
			}
		}
	}

	private void createAndSaveDraft(Activity activity, Farm farm, ProductionUnit pu, CropType crop, Integer userId) {
		ActivityDraft draft = new ActivityDraft();
		draft.setActivity(activity);
		draft.setFarm(farm);
		draft.setProductionUnit(pu);
		draft.setCropType(crop);
		draft.setUserId(userId);
		draft.setCreatedAt(LocalDateTime.now());
		draft.setCreatedBy(userId);

		activityDraftRepository.save(draft);
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

		boolean hasSpecificUnits = productionUnitIds != null && !productionUnitIds.isEmpty();

		List<Farm> farmsToProcess;
		if (targetFarms != null && !targetFarms.isEmpty()) {
			farmsToProcess = targetFarms;
		} else {
			farmsToProcess = farmRepository.findByCompanyIdAndIsActiveTrue(company.getId());
		}

		for (Farm farm : farmsToProcess) {
			List<FarmCrop> farmCrops = farmCropRepository.findByFarmId(farm.getId());
			boolean hasCrop = farmCrops.stream().anyMatch(fc -> activityCropTypeIds.contains(fc.getCropType().getId()));

			if (!hasCrop) {
				continue;
			}

			if (hasSpecificUnits) {
				// Units desta farm que estão na lista e são compatíveis
				List<ProductionUnit> unitsForThisFarm = productionUnitRepository
						.findByIdInAndFarmIdAndCropTypesCompatible(productionUnitIds, farm.getId(),
								activityCropTypeIds);

				if (unitsForThisFarm.isEmpty()) {
					continue; // Se não tem UP compatível selecionada nesta farm, pula
				}

				for (ProductionUnit pu : unitsForThisFarm) {
					UserActivity ua = new UserActivity();
					ua.setActivity(activity);
					ua.setUser(farm.getOwner());
					ua.setFarm(farm);
					ua.setProductionUnit(pu);
					ua.setStatus(pendingStatus);
					ua.setCreatedAt(LocalDateTime.now());
					ua.setCreatedBy(userCreatedBy);
					userActivityRepository.save(ua);
				}

			} else {
				// Sem UP específica (comportamento padrão)
				List<ProductionUnit> units = productionUnitRepository.findByFarmAndCropTypesCompatible(farm.getId(),
						activityCropTypeIds);

				if (units.isEmpty()) {
					UserActivity ua = new UserActivity();
					ua.setActivity(activity);
					ua.setUser(farm.getOwner());
					ua.setFarm(farm);
					ua.setStatus(pendingStatus);
					ua.setCreatedAt(LocalDateTime.now());
					ua.setCreatedBy(userCreatedBy);
					userActivityRepository.save(ua);
				} else {
					for (ProductionUnit pu : units) {
						UserActivity ua = new UserActivity();
						ua.setActivity(activity);
						ua.setUser(farm.getOwner());
						ua.setFarm(farm);
						ua.setProductionUnit(pu);
						ua.setStatus(pendingStatus);
						ua.setCreatedAt(LocalDateTime.now());
						ua.setCreatedBy(userCreatedBy);
						userActivityRepository.save(ua);
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

		// 8. Se for draft, atualizar também a tabela activity_draft (caso o update mude
		// farms/UPs)
		// Se o seu CreateActivityDTO tiver farmIds e productionUnitIds, adicione a
		// lógica aqui.
		// Se o DTO não tiver, você perde essa atualização no draft.
		// Vou deixar comentado como sugestão:
		/*
		 * if (dto.getFarmIds() != null) { // se vier no DTO de update // Recalcular
		 * farms e UPs igual no register // saveDraftSelections(savedActivity, newFarms,
		 * newProductionUnits, updatedBy); }
		 */

		Map<String, Object> response = new HashMap<>();
		response.put("id", savedActivity.getId());
		response.put("message", "Atividade atualizada com sucesso!");
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
			List<Integer> farmIds,
			List<Integer> productionUnitIds,
			LocalDate startDate, LocalDate endDate, int page, int size) {

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

	private void validateProductionUnitsBelongToFarmsAndCrops(List<Integer> unitIds, List<Integer> farmIds,
			List<Integer> cropTypeIds, Integer companyId) {

		List<ProductionUnit> units = productionUnitRepository.findByIdInAndIsActiveTrue(unitIds);
		if (units.size() != unitIds.size()) {
			throw new BusinessException("Uma ou mais unidades produtivas são inválidas ou inativas.");
		}

		for (ProductionUnit pu : units) {
			if (!farmIds.contains(pu.getFarm().getId())) {
				throw new BusinessException(
						"Unidade produtiva " + pu.getId() + " não pertence às fazendas selecionadas.");
			}
		}

		List<Integer> compatibleIds = productionUnitRepository.findCompatibleUnitIdsByCropTypes(unitIds, cropTypeIds);

		if (compatibleIds.size() != unitIds.size()) {
			throw new BusinessException(
					"Uma ou mais unidades produtivas não são compatíveis com as culturas selecionadas.");
		}
	}

}