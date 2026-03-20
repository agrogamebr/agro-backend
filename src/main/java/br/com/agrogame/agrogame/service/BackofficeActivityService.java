package br.com.agrogame.agrogame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.ActivityDecisionRequestDTO;
import br.com.agrogame.agrogame.dto.ActivityDecisionResponseDTO;
import br.com.agrogame.agrogame.dto.BackofficeActivityListDTO;
import br.com.agrogame.agrogame.dto.BackofficeActivityProjection;
import br.com.agrogame.agrogame.dto.BackofficeFileInfoDTO;
import br.com.agrogame.agrogame.dto.BackofficeSubmissionListDTO;
import br.com.agrogame.agrogame.dto.UserActivityDetailDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.ReviewStatus;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityReview;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.model.UserActivitySubmission;
import br.com.agrogame.agrogame.model.UserActivitySubmissionFile;
import br.com.agrogame.agrogame.model.UserDocument;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.ReviewStatusRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserActivityReviewRepository;
import br.com.agrogame.agrogame.repository.UserActivityStatusRepository;
import br.com.agrogame.agrogame.repository.UserActivitySubmissionFileRepository;
import br.com.agrogame.agrogame.repository.UserActivitySubmissionRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BackofficeActivityService {

	private final UserRepository userRepository;
	private final UserActivityRepository userActivityRepository;
	private final UserActivitySubmissionFileRepository submissionFileRepository;
	private final UserActivityStatusRepository userActivityStatusRepository;
	private final UserDocumentRepository userDocumentRepository;
	private final UserActivitySubmissionRepository userActivitySubmissionRepository;
	private final ReviewStatusRepository reviewStatusRepository;
	private final UserActivityReviewRepository userActivityReviewRepository;
	private final PointsAndRewardsService pointsAndRewardsService;
	private final ActivityRepository activityRepository;
	private final UserActivityService userActivityService;

	public BackofficeActivityService(UserRepository userRepository, UserActivityRepository userActivityRepository,
			UserActivitySubmissionFileRepository submissionFileRepository,
			UserActivityStatusRepository userActivityStatusRepository, UserDocumentRepository userDocumentRepository,
			UserActivitySubmissionRepository userActivitySubmissionRepository,
			ReviewStatusRepository reviewStatusRepository, UserActivityReviewRepository userActivityReviewRepository,
			PointsAndRewardsService pointsAndRewardsService, ActivityRepository activityRepository, UserActivityService userActivityService) {
		this.userRepository = userRepository;
		this.userActivityRepository = userActivityRepository;
		this.submissionFileRepository = submissionFileRepository;
		this.userActivityStatusRepository = userActivityStatusRepository;
		this.userDocumentRepository = userDocumentRepository;
		this.userActivitySubmissionRepository = userActivitySubmissionRepository;
		this.reviewStatusRepository = reviewStatusRepository;
		this.userActivityReviewRepository = userActivityReviewRepository;
		this.pointsAndRewardsService = pointsAndRewardsService;
		this.activityRepository = activityRepository;
		this.userActivityService = userActivityService;
	}

	/**
	 * Lista atividades submetidas para aprovação do backoffice
	 * 
	 * @param backofficeUserId ID do usuário backoffice autenticado
	 * @return Lista de atividades submetidas da empresa do usuário
	 * @throws AccessDeniedException
	 */
	public Page<BackofficeSubmissionListDTO> listSubmittedActivities(Integer backofficeUserId, int page, int size) {

		User backofficeUser = userRepository.findById(backofficeUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Integer userTypeId = backofficeUser.getUserType().getId();
		if (!isBackofficeUserType(userTypeId)) {
			throw new AccessDeniedException(
					"Apenas usuários com permissão de backoffice (tipos 1,2,3,6) podem acessar");
		}

		Integer companyId = backofficeUser.getCompany().getId();
		if (companyId == null) {
			throw new BusinessException("Usuário backoffice não está vinculado a uma empresa");
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<UserActivity> submittedActivitiesPage = userActivityRepository.findSubmittedByCompanyId(companyId,
				pageable);

		return submittedActivitiesPage.map(this::toBackofficeSubmissionDTO);
	}

	/**
	 * Converte UserActivity para DTO com todos os detalhes necessários
	 */
	private BackofficeSubmissionListDTO toBackofficeSubmissionDTO(UserActivity userActivity) {

		// Buscar dados relacionados
		Activity activity = userActivity.getActivity();
		Farm farm = userActivity.getFarm();
		User producer = userActivity.getUser();

		// Buscar arquivos vinculados
		List<UserActivitySubmissionFile> files = submissionFileRepository.findByUserActivityId(userActivity.getId());

		// Buscar submissão mais recente para pegar uploadedAt
		LocalDateTime uploadedAt = findLastSubmissionCreatedAt(userActivity.getId());

		// Converter arquivos para DTO
		List<BackofficeFileInfoDTO> filesList = files.stream().map(file -> new BackofficeFileInfoDTO(file.getId(),
				extractFileName(file.getFileUrl()), file.getFileUrl(), file.getGsutilUri(), uploadedAt)).toList();

		// Montar DTO
		BackofficeSubmissionListDTO dto = new BackofficeSubmissionListDTO();
		dto.setUserActivityId(userActivity.getId());
		dto.setActivityId(activity.getId());
		dto.setActivityName(activity.getName());
		dto.setFarmId(farm.getId());
		dto.setFarmName(farm.getName());
		dto.setProducerId(producer.getId());
		dto.setProducerName(producer.getFullName());
		String producerDocument = findProducerDocument(producer.getId());

		dto.setProducerDocument(producerDocument);
		dto.setSubmittedAt(userActivity.getCreatedAt()); // Ou submittedAt se tiver
		dto.setFilesCount(files.size());
		dto.setStatus("submitted");
		dto.setFiles(filesList);
		dto.setPoints(activity.getPoints());
		dto.setDescription(activity.getDescription());

		return dto;
	}

	/**
	 * Valida se o user_type é permitido para backoffice
	 */
	private boolean isBackofficeUserType(Integer userTypeId) {
		return userTypeId != null && (userTypeId == 1 || userTypeId == 2 || userTypeId == 3 || userTypeId == 6);
	}

	private void validateBackofficeUserType(User user) {
		if (user.getUserType() == null || user.getUserType().getId() == null) {
			throw new AccessDeniedException("Usuário sem tipo definido");
		}

		Integer userTypeId = user.getUserType().getId();
		if (!isBackofficeUserType(userTypeId)) {
			throw new AccessDeniedException(String.format(
					"Usuário tipo %d não possui permissão de backoffice. Apenas tipos 1,2,3,6 têm acesso.",
					userTypeId));
		}
	}

	/**
	 * Extrai apenas o nome do arquivo da URL Ex:
	 * "https://bucket.storage.google.com/submissions/uuid.pdf" → "uuid.pdf"
	 */
	private String extractFileName(String fileUrl) {
		if (fileUrl == null || fileUrl.isBlank()) {
			return "unknown";
		}
		return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
	}

	private String findProducerDocument(Integer producerId) {
		return userDocumentRepository.findFirstByUserIdAndIsPrimaryTrueAndIsActiveTrue(producerId)
				.or(() -> userDocumentRepository.findFirstCpfByUserId(producerId)).map(UserDocument::getDocumentNumber)
				.orElse(null);
	}

	private LocalDateTime findLastSubmissionCreatedAt(Integer userActivityId) {
		return userActivitySubmissionRepository.findByUserActivityIdOrderByCreatedAtDesc(userActivityId).stream()
				.findFirst().map(UserActivitySubmission::getCreatedAt) // ou getSubmittedAt()
				.orElse(null);
	}

	public ActivityDecisionResponseDTO makeDecision(Integer userActivityId, Integer backofficeUserId,
			ActivityDecisionRequestDTO decisionRequest) {

		// 1. Validar usuário backoffice
		User backofficeUser = userRepository.findById(backofficeUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		validateBackofficeUserType(backofficeUser);

		// 2. Buscar UserActivity
		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("UserActivity não encontrada"));

		// 3. Validar empresa
		if (!userActivity.getActivity().getCompany().getId().equals(backofficeUser.getCompany().getId())) {
			throw new AccessDeniedException("Atividade não pertence à empresa do usuário backoffice");
		}

		// 4. Validar status
		if (!"submitted".equals(userActivity.getStatus().getCode())) {
			throw new BusinessException("Só é possível processar atividades em status 'submitted'. " + "Status atual: "
					+ userActivity.getStatus().getCode());
		}

		// 5. Processar decision
		String raw = decisionRequest.getDecision();
		if (raw == null || raw.isBlank()) {
			throw new BusinessException("Decision não pode ser nula ou vazia");
		}

		String decision = raw.toLowerCase();
		if (!"approved".equals(decision) && !"rejected".equals(decision)) {
			throw new BusinessException("Decision inválida: " + raw + ". Use 'approved' ou 'rejected'");
		}

		UserActivityStatus newStatus = userActivityStatusRepository.findByCode(decision)
				.orElseThrow(() -> new ResourceNotFoundException("Status '" + decision + "' não configurado no banco"));

		userActivity.setStatus(newStatus);
		if ("approved".equals(decision)) {
			userActivity.setCompletedAt(LocalDateTime.now());
		}
		userActivity.setUpdatedBy(backofficeUser);
		userActivity.setUpdatedAt(LocalDateTime.now());

		UserActivity savedActivity = userActivityRepository.save(userActivity);

		// Review
		ReviewStatus reviewStatus = reviewStatusRepository.findByCode(decision).orElseThrow(
				() -> new ResourceNotFoundException("ReviewStatus '" + decision + "' não configurado no banco"));

		UserActivityReview review = new UserActivityReview();
		review.setUserActivity(savedActivity);
		review.setReviewer(backofficeUser);
		review.setReviewStatus(reviewStatus);
		review.setReviewNotes(decisionRequest.getReason());
		review.setReviewedAt(LocalDateTime.now());
		review.setCreatedBy(backofficeUser.getId());
		review.setCreatedAt(LocalDateTime.now());
		review.setUpdatedBy(backofficeUser.getId());
		review.setUpdatedAt(LocalDateTime.now());

		userActivityReviewRepository.save(review);

		if ("approved".equals(decision)) {
			pointsAndRewardsService.creditOnActivityApproval(savedActivity, backofficeUser);
		}

		return new ActivityDecisionResponseDTO(savedActivity.getId(), savedActivity.getActivity().getId(),
				savedActivity.getActivity().getName(), decision, newStatus.getCode(), LocalDateTime.now(),
				decisionRequest.getReason());
	}
	
	
	@Transactional(readOnly = true)
	public UserActivityDetailDTO getActivityDetailForBackoffice(User backofficeUser, Integer userActivityId) {
	    
	    // 1. Busca a atividade
	    UserActivity userActivity = userActivityRepository.findById(userActivityId)
	            .orElseThrow(() -> new ResourceNotFoundException("User Activity não encontrada"));

	    // 2. Valida se o produtor dono da atividade pertence à MESMA EMPRESA do usuário do backoffice
	    if (!userActivity.getUser().getCompany().getId().equals(backofficeUser.getCompany().getId())) {
	        throw new AccessDeniedException("Esta atividade pertence a um produtor de outra empresa.");
	    }

	    // 3. Busca as reviews (se houver necessidade de exibi-las também)
	    List<UserActivityReview> reviews = userActivityReviewRepository
	            .findByUserActivityIdOrderByReviewedAtDesc(userActivityId);

	    // 4. Reaproveita o seu método toDto que você já possui para converter e retornar
	    return userActivityService.toDto(userActivity, reviews);
	}

	public Page<BackofficeActivityListDTO> listActivitiesForBackoffice(Integer companyId, String activityStatus,
			String userActivityStatus, Integer producerId, Integer farmId, Integer productionUnitId, Integer cropTypeId,
			LocalDate startDate, LocalDate endDate, User backofficeUser, int page, int size) {
		
		validateBackofficeUserType(backofficeUser);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "validFrom"));

		Page<BackofficeActivityProjection> pageResult = activityRepository.findForBackoffice(companyId, activityStatus,
				userActivityStatus, producerId, farmId, productionUnitId, cropTypeId, startDate, endDate, pageable);

		return pageResult.map(this::toBackofficeActivityListDTO);
	}

	private BackofficeActivityListDTO toBackofficeActivityListDTO(BackofficeActivityProjection p) {
		BackofficeActivityListDTO dto = new BackofficeActivityListDTO();
		dto.setId(p.getId());
		dto.setName(p.getName());
		dto.setDescription(p.getDescription());
		dto.setPoints(p.getPoints());
		dto.setStatusCode(p.getStatus());
		dto.setValidFrom(p.getValidFrom());
		dto.setValidTo(p.getValidTo());
		dto.setThumbnailUrl(p.getThumbnailUrl());
		dto.setThumbnailGsutilUri(p.getThumbnailGsutilUri());

		dto.setUserActivityId(p.getUserActivityId());
		dto.setUserActivityStatus(p.getUserActivityStatus());
		dto.setProducerId(p.getUserId());
		dto.setFarmId(p.getFarmId());
		dto.setProductionUnitId(p.getProductionUnitId());

		return dto;
	}

}
