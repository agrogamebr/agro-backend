package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

import br.com.agrogame.agrogame.dto.FileUploadResponseDTO;
import br.com.agrogame.agrogame.dto.SubmitActivityResponseDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.model.UserActivitySubmission;
import br.com.agrogame.agrogame.model.UserActivitySubmissionFile;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserActivityStatusRepository;
import br.com.agrogame.agrogame.repository.UserActivitySubmissionFileRepository;
import br.com.agrogame.agrogame.repository.UserActivitySubmissionRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.StoredFileInfo;

@Service
public class ActivitySubmissionService {

	private final UserActivityRepository userActivityRepository;
	private final UserActivitySubmissionRepository userActivitySubmissionRepository;
	private final UserActivityStatusRepository userActivityStatusRepository;
	private final ActivityRepository activityRepository;
	private final FarmRepository farmRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final Storage storage;
	private final UserActivitySubmissionFileRepository userActivitySubmissionFileRepository;

	public ActivitySubmissionService(UserActivityRepository userActivityRepository,
			UserActivitySubmissionRepository userActivitySubmissionRepository,
			UserActivityStatusRepository userActivityStatusRepository, ActivityRepository activityRepository,
			FarmRepository farmRepository, UserRepository userRepository, FileStorageService fileStorageService,
			Storage storage, UserActivitySubmissionFileRepository userActivitySubmissionFileRepository) {
		this.userActivityRepository = userActivityRepository;
		this.userActivitySubmissionRepository = userActivitySubmissionRepository;
		this.userActivityStatusRepository = userActivityStatusRepository;
		this.activityRepository = activityRepository;
		this.farmRepository = farmRepository;
		this.userRepository = userRepository;
		this.fileStorageService = fileStorageService;
		this.storage = storage;
		this.userActivitySubmissionFileRepository = userActivitySubmissionFileRepository;
	}

	@Transactional
	public FileUploadResponseDTO uploadFile(Integer producerId, Integer userActivityId, MultipartFile file,
			String description) throws IOException {

		// 1. Validar produtor
		User producer = userRepository.findById(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		if (producer.getUserType() == null || !producer.getUserType().getId().equals(8)) {
			throw new AccessDeniedException("Usuário não é produtor rural");
		}

		Integer companyId = producer.getCompany().getId();

		// 2. Buscar UserActivity pelo ID
		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("UserActivity não encontrada"));

		// 3. Segurança: garantir vínculo com produtor, fazenda e empresa
		if (!userActivity.getUser().getId().equals(producerId)) {
			throw new AccessDeniedException("Atividade não pertence a este produtor");
		}

		if (!userActivity.getFarm().getCompany().getId().equals(companyId)) {
			throw new AccessDeniedException("Atividade não pertence à empresa do produtor");
		}

		// 4. Validar status da Activity (send)
		Activity activity = userActivity.getActivity();
		if (!"send".equals(activity.getActivityStatus().getCode())) {
			throw new BusinessException("Atividade não está disponível para submissão");
		}

		// 5. Validar que UserActivity está pending
		if (!"pending".equals(userActivity.getStatus().getCode())) {
			throw new BusinessException("Atividade já foi submetida ou processada e não pode receber novos arquivos");
		}

		// 6. Upload do arquivo (GCS)
		StoredFileInfo stored = fileStorageService.uploadFile(file);
		String fileHash = fileStorageService.generateFileHash(file);

		// 7. Criar registro em user_activity_submission_files
		UserActivitySubmissionFile fileEntity = new UserActivitySubmissionFile();
		fileEntity.setUserActivity(userActivity);
		fileEntity.setFileUrl(stored.getFileUrl());
		fileEntity.setFileHash(fileHash);
		fileEntity.setGsutilUri(stored.getGsutilUri());
		userActivitySubmissionFileRepository.save(fileEntity);

		// 8. Criar registro em UserActivitySubmission
		UserActivitySubmission submission = new UserActivitySubmission();
		submission.setUserActivity(userActivity);
		submission.setDescription(description);
		submission.setCreatedAt(LocalDateTime.now());
		submission.setCreatedBy(producer);

		UserActivitySubmission savedSubmission = userActivitySubmissionRepository.save(submission);

		return new FileUploadResponseDTO(savedSubmission.getId(), file.getOriginalFilename(), stored.getFileUrl(),
				description, savedSubmission.getCreatedAt());
	}

	@Transactional
	public SubmitActivityResponseDTO submitActivity(Integer producerId, Integer userActivityId) {

		// 1. Validar produtor
		User producer = userRepository.findById(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		if (producer.getUserType() == null || !producer.getUserType().getId().equals(8)) {
			throw new AccessDeniedException("Usuário não é produtor rural");
		}

		Integer companyId = producer.getCompany().getId();

		// 2. Buscar UserActivity
		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("UserActivity não encontrada"));

		// 3. Garantir vínculo com produtor e empresa
		if (!userActivity.getUser().getId().equals(producerId)) {
			throw new AccessDeniedException("Atividade não pertence a este produtor");
		}

		if (!userActivity.getFarm().getCompany().getId().equals(companyId)) {
			throw new AccessDeniedException("Atividade não pertence à empresa do produtor");
		}

		Activity activity = userActivity.getActivity();

		// 4. Validar status da UserActivity
		if (!"pending".equals(userActivity.getStatus().getCode())) {
			throw new BusinessException("Atividade já foi submetida ou processada");
		}

		// 5. Validar que existe pelo menos 1 arquivo
		List<UserActivitySubmission> submissions = userActivitySubmissionRepository
				.findByUserActivityId(userActivity.getId());

		if (submissions.isEmpty()) {
			throw new BusinessException("É obrigatório enviar pelo menos um arquivo antes de submeter");
		}

		// 6. Mudar status para submitted
		UserActivityStatus submittedStatus = userActivityStatusRepository.findByCode("submitted")
				.orElseThrow(() -> new ResourceNotFoundException("Status 'submitted' não configurado"));

		userActivity.setStatus(submittedStatus);
		userActivity.setUpdatedAt(LocalDateTime.now());
		userActivity.setUpdatedBy(producer);

		UserActivity savedUserActivity = userActivityRepository.save(userActivity);

		// 7. Atualizar submittedAt nos submissions
		LocalDateTime now = LocalDateTime.now();
		for (UserActivitySubmission submission : submissions) {
			submission.setSubmittedAt(now);
			submission.setUpdatedAt(now);
			submission.setUpdatedBy(producer);
			userActivitySubmissionRepository.save(submission);
		}

		return new SubmitActivityResponseDTO(savedUserActivity.getId(), activity.getId(),
				userActivity.getFarm().getId(), submittedStatus.getCode(), submissions.size(), now,
				"Atividade submetida com sucesso! Aguarde análise da empresa.");
	}

	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> downloadSubmissionFile(Integer producerId, Integer userActivityId,
			Integer submissionFileId) {

		// 1. Validar produtor
		User producer = userRepository.findById(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		if (producer.getUserType() == null || !producer.getUserType().getId().equals(8)) {
			throw new AccessDeniedException("Usuário não é produtor rural");
		}

		Integer companyId = producer.getCompany().getId();

		// 2. Buscar UserActivity
		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("UserActivity não encontrada"));

		if (!userActivity.getUser().getId().equals(producerId)) {
			throw new AccessDeniedException("Atividade não pertence a este produtor");
		}

		if (!userActivity.getFarm().getCompany().getId().equals(companyId)) {
			throw new AccessDeniedException("Atividade não pertence à empresa do produtor");
		}

		// 3. Buscar arquivo e garantir vínculo
		UserActivitySubmissionFile fileEntity = userActivitySubmissionFileRepository.findById(submissionFileId)
				.orElseThrow(() -> new ResourceNotFoundException("Arquivo da submissão não encontrado"));

		if (!fileEntity.getUserActivity().getId().equals(userActivity.getId())) {
			throw new AccessDeniedException("Arquivo não pertence a esta submissão");
		}

		// 6. Usar gsutil_uri para obter bucket e objectName
		String gsutilUri = fileEntity.getGsutilUri();

		if (gsutilUri == null || !gsutilUri.startsWith("gs://")) {
			throw new BusinessException("gsutil_uri inválido");
		}

		String withoutScheme = gsutilUri.substring("gs://".length());
		int slashIdx = withoutScheme.indexOf('/');
		if (slashIdx == -1) {
			throw new BusinessException("gsutil_uri inválido");
		}

		String bucket = withoutScheme.substring(0, slashIdx);
		String objectName = withoutScheme.substring(slashIdx + 1);

		// 7. Ler do GCS
		Blob blob = storage.get(BlobId.of(bucket, objectName));
		if (blob == null || !blob.exists()) {
			throw new ResourceNotFoundException("Arquivo não encontrado no storage");
		}

		byte[] content = blob.getContent();

		String filename = objectName.contains("/") ? objectName.substring(objectName.lastIndexOf('/') + 1) : objectName;

		String contentType = blob.getContentType() != null ? blob.getContentType() : "application/octet-stream";

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
				.header("Content-Length", String.valueOf(content.length))
				.contentType(org.springframework.http.MediaType.parseMediaType(contentType)).body(content);
	}

}
