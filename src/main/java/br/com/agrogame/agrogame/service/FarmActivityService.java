package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.CreateFarmActivityDTO;
import br.com.agrogame.agrogame.dto.FarmActivityResponseDTO;
import br.com.agrogame.agrogame.model.ActivityStatus;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.FarmActivity;
import br.com.agrogame.agrogame.repository.ActivityStatusRepository;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.FarmActivityRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;

@Service
public class FarmActivityService {

	private final FarmActivityRepository activityRepo;
	private final FarmRepository farmRepo;
	private final CropTypeRepository cropTypeRepo;
	private final ActivityStatusRepository statusRepo;

	public FarmActivityService(FarmActivityRepository activityRepo, FarmRepository farmRepo,
			CropTypeRepository cropTypeRepo, ActivityStatusRepository statusRepo) {
		this.activityRepo = activityRepo;
		this.farmRepo = farmRepo;
		this.cropTypeRepo = cropTypeRepo;
		this.statusRepo = statusRepo;
	}

	@Transactional
	public FarmActivityResponseDTO registerFarmActivity(CreateFarmActivityDTO dto, Integer createdBy) {

		// Validar farm
		Farm farm = farmRepo.findById(dto.getFarmId())
				.orElseThrow(() -> new IllegalArgumentException("Fazenda não encontrada"));

		// Validar cropType (opcional)
		CropType cropType = null;
		if (dto.getCropTypeId() != null) {
			cropType = cropTypeRepo.findById(dto.getCropTypeId())
					.orElseThrow(() -> new IllegalArgumentException("Tipo de cultura não encontrado"));
		}

		// Validar status
		ActivityStatus status = statusRepo.findById(dto.getStatusId())
				.orElseThrow(() -> new IllegalArgumentException("Status não encontrado"));

		// Criar entidade
		FarmActivity activity = new FarmActivity();
		activity.setFarm(farm);
		activity.setCropType(cropType);
		activity.setActivityType(dto.getActivityType());
		activity.setName(dto.getName());
		activity.setDescription(dto.getDescription());
		activity.setScheduledDate(dto.getScheduledDate());
		activity.setStatus(status);
		activity.setCreatedAt(LocalDateTime.now());
		activity.setCreatedBy(createdBy);

		FarmActivity saved = activityRepo.save(activity);

		// Montar response
		return new FarmActivityResponseDTO(saved.getId(), saved.getFarm().getId(), saved.getFarm().getName(),
				saved.getCropType() != null ? saved.getCropType().getId() : null,
				saved.getCropType() != null ? saved.getCropType().getName() : "N/A", saved.getActivityType(),
				saved.getName(), saved.getDescription(), saved.getScheduledDate(), saved.getStatus().getCode(),
				saved.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
				"Atividade de fazenda cadastrada com sucesso");
	}
}
