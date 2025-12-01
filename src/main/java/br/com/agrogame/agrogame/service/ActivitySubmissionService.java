package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.FileUploadResponseDTO;
import br.com.agrogame.agrogame.dto.SubmitActivityResponseDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.model.UserActivitySubmission;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserActivityStatusRepository;
import br.com.agrogame.agrogame.repository.UserActivitySubmissionRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class ActivitySubmissionService {

    private final UserActivityRepository userActivityRepository;
    private final UserActivitySubmissionRepository userActivitySubmissionRepository;
    private final UserActivityStatusRepository userActivityStatusRepository;
    private final ActivityRepository activityRepository;
    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ActivitySubmissionService(UserActivityRepository userActivityRepository,
                                     UserActivitySubmissionRepository userActivitySubmissionRepository,
                                     UserActivityStatusRepository userActivityStatusRepository,
                                     ActivityRepository activityRepository,
                                     FarmRepository farmRepository,
                                     UserRepository userRepository,
                                     FileStorageService fileStorageService) {
        this.userActivityRepository = userActivityRepository;
        this.userActivitySubmissionRepository = userActivitySubmissionRepository;
        this.userActivityStatusRepository = userActivityStatusRepository;
        this.activityRepository = activityRepository;
        this.farmRepository = farmRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FileUploadResponseDTO uploadFile(Integer producerId, Integer activityId, Integer farmId,
                                            MultipartFile file, String description) throws IOException {

        // 1. Validar produtor
        User producer = userRepository.findById(producerId)
            .orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

        if (producer.getUserType() == null || !producer.getUserType().getId().equals(8)) {
            throw new AccessDeniedException("Usuário não é produtor rural");
        }

        Integer companyId = producer.getCompany().getId();

        // 2. Validar atividade
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

        if (!activity.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Atividade não pertence à empresa do produtor");
        }

        if (!"send".equals(activity.getActivityStatus().getCode())) {
            throw new BusinessException("Atividade não está disponível para submissão");
        }

        // 3. Validar fazenda
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

        if (!farm.getOwner().getId().equals(producerId) || !farm.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Fazenda não pertence ao produtor ou empresa");
        }

        // 4. Obter ou criar UserActivity
        UserActivity userActivity = userActivityRepository
            .findByUserAndActivityAndFarm(producerId, activityId, farmId)
            .orElseGet(() -> {
                UserActivityStatus pendingStatus = userActivityStatusRepository.findByCode("pending")
                    .orElseThrow(() -> new ResourceNotFoundException("Status 'pending' não configurado"));

                UserActivity ua = new UserActivity();
                ua.setUser(producer);
                ua.setActivity(activity);
                ua.setFarm(farm);
                ua.setStatus(pendingStatus);
                ua.setCreatedAt(LocalDateTime.now());
                ua.setCreatedBy(producer);

                return userActivityRepository.save(ua);
            });

        // 5. Validar que não foi submetida ainda (não pode estar em submitted/approved/rejected)
        if (!"pending".equals(userActivity.getStatus().getCode())) {
            throw new BusinessException("Atividade já foi submetida ou processada e não pode receber novos arquivos");
        }

        // 6. Upload do arquivo
        String fileUrl = fileStorageService.uploadFile(file);
        String fileHash = fileStorageService.generateFileHash(file);

        // 7. Criar registro em UserActivitySubmission
        UserActivitySubmission submission = new UserActivitySubmission();
        submission.setUserActivity(userActivity);
        submission.setFileUrl(fileUrl);
        submission.setFileHash(fileHash);
        submission.setDescription(description);
        submission.setCreatedAt(LocalDateTime.now());
        submission.setCreatedBy(producer);

        UserActivitySubmission savedSubmission = userActivitySubmissionRepository.save(submission);

        // 8. Retornar resposta
        return new FileUploadResponseDTO(
            savedSubmission.getId(),
            file.getOriginalFilename(),
            fileUrl,
            description,
            savedSubmission.getCreatedAt()
        );
    }

    @Transactional
    public SubmitActivityResponseDTO submitActivity(Integer producerId, Integer activityId, Integer farmId) {

        // 1. Validar produtor
        User producer = userRepository.findById(producerId)
            .orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

        if (producer.getUserType() == null || !producer.getUserType().getId().equals(8)) {
            throw new AccessDeniedException("Usuário não é produtor rural");
        }

        Integer companyId = producer.getCompany().getId();

        // 2. Validar atividade
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

        if (!activity.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Atividade não pertence à empresa do produtor");
        }

        // 3. Validar fazenda
        Farm farm = farmRepository.findById(farmId)
            .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

        if (!farm.getOwner().getId().equals(producerId) || !farm.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Fazenda não pertence ao produtor ou empresa");
        }

        // 4. Buscar UserActivity
        UserActivity userActivity = userActivityRepository
            .findByUserAndActivityAndFarm(producerId, activityId, farmId)
            .orElseThrow(() -> new ResourceNotFoundException("Nenhuma atividade encontrada para este contexto"));

        // 5. Validar status = pending (ainda não foi submetida)
        if (!"pending".equals(userActivity.getStatus().getCode())) {
            throw new BusinessException("Atividade já foi submetida ou processada");
        }

        // 6. Validar que existe pelo menos 1 arquivo
        List<UserActivitySubmission> submissions = userActivitySubmissionRepository
            .findByUserActivityId(userActivity.getId());

        if (submissions.isEmpty()) {
            throw new BusinessException("É obrigatório enviar pelo menos um arquivo antes de submeter");
        }

        // 7. Mudar status para submitted
        UserActivityStatus submittedStatus = userActivityStatusRepository.findByCode("submitted")
            .orElseThrow(() -> new ResourceNotFoundException("Status 'submitted' não configurado"));

        userActivity.setStatus(submittedStatus);
        userActivity.setUpdatedAt(LocalDateTime.now());
        userActivity.setUpdatedBy(producer);

        UserActivity savedUserActivity = userActivityRepository.save(userActivity);

        // 8. Atualizar submittedAt em todos os submissions (ou deixar null, conforme design)
        LocalDateTime now = LocalDateTime.now();
        for (UserActivitySubmission submission : submissions) {
            submission.setSubmittedAt(now);
            submission.setUpdatedAt(now);
            submission.setUpdatedBy(producer);
            userActivitySubmissionRepository.save(submission);
        }

        // 9. Retornar resposta
        return new SubmitActivityResponseDTO(
            savedUserActivity.getId(),
            activityId,
            farmId,
            submittedStatus.getCode(),
            submissions.size(),
            now,
            "Atividade submetida com sucesso! Aguarde análise da empresa."
        );
    }
}
