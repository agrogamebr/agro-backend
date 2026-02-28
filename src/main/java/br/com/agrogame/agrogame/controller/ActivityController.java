package br.com.agrogame.agrogame.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.ActivityListDTO;
import br.com.agrogame.agrogame.dto.CreateActivityDTO;
import br.com.agrogame.agrogame.dto.CreateActivityMultipartDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.ActivityRepository;
import br.com.agrogame.agrogame.repository.ActivityRewardRepository;
import br.com.agrogame.agrogame.repository.RewardRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activity")
@Tag(name = "Activity", description = "Endpoints para cadastro de atividades")
public class ActivityController {

	private final ActivityService service;
	private final UserRepository userRepository;

	public ActivityController(ActivityService service, UserRepository userRepository,
			ActivityRewardRepository activityRewardRepository, ActivityRepository activityRepository,
			RewardRepository rewardRepository) {
		this.service = service;
		this.userRepository = userRepository;
	}

	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> listActivities(
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) List<Integer> cropTypeIds,
	        @RequestParam(required = false) List<Integer> farmIds,
	        @RequestParam(required = false) List<Integer> productionUnitIds,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "20") int size,
	        Principal principal) {

	    Integer companyId = userRepository.findByEmail1(principal.getName())
	            .orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getCompany().getId();
	    
	    User user = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

	    Page<ActivityListDTO> activitiesPage = service.listActivities(
	            companyId, status, cropTypeIds, farmIds, productionUnitIds, startDate, endDate, user, page, size);

	    Map<String, Object> response = new HashMap<>();
	    response.put("activities", activitiesPage.getContent());
	    response.put("page", activitiesPage.getNumber());
	    response.put("size", activitiesPage.getSize());
	    response.put("totalElements", activitiesPage.getTotalElements());
	    response.put("totalPages", activitiesPage.getTotalPages());

	    return ResponseEntity.ok(response);
	}


	@PostMapping(value = "/create-activity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> createActivity(
			@Valid @ModelAttribute CreateActivityMultipartDTO multipartDto, Principal principal) throws IOException {

		// 1. Busca o usuário logado
		User currentUser = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		// 2. Validação de Segurança: O usuário pertence a uma empresa?
		if (currentUser.getCompany() == null) {
			throw new BusinessException("Usuário não vinculado a nenhuma empresa. Não pode criar atividades.");
		}

		// 3. Passa a Empresa e o ID do Usuário para o Service
		Map<String, Object> response = service.registerActivity(multipartDto, currentUser);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Buscar atividade", description = "Retorna os detalhes de uma atividade para edição")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade encontrada"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@GetMapping("/{activityId}")
	public ResponseEntity<ActivityListDTO> getActivity(@PathVariable Integer activityId) {
	    ActivityListDTO activity = service.listActivity(activityId);
	    
	    return ResponseEntity.ok(activity);
	}

	@PutMapping("/{activityId}")
	@Operation(summary = "Editar atividade", description = "Edita uma atividade que está em status 'draft'")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade atualizada"),
			@ApiResponse(responseCode = "400", description = "Atividade não está em draft"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	public ResponseEntity<Map<String, Object>> updateActivity(@PathVariable Integer activityId,
			@Valid @RequestBody CreateActivityDTO dto, Principal principal) {
		
		User currentUser = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Map<String, Object> response = service.updateActivity(activityId, dto, currentUser);
		return ResponseEntity.ok(response);
	}

	@PostMapping(value = "/activities/{activityId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> updateActivityThumbnail(@PathVariable Integer activityId,
			@RequestPart("thumbnail") MultipartFile thumbnail, Principal principal) throws IOException {

		User currentUser = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Map<String, Object> response = service.updateActivityThumbnail(activityId, thumbnail, currentUser);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{activityId}/send")
	@Operation(summary = "Enviar atividade", description = "Altera o status de uma atividade de 'draft' para 'send', "
			+ "bloqueando novas edições e disparando o fluxo de envio para os producers elegíveis.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade enviada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Apenas atividades em status 'draft' podem ser enviadas"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao enviar atividade") })
	public ResponseEntity<Map<String, Object>> sendActivity(@PathVariable Integer activityId, Principal principal) {

		Integer sentBy = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado")).getId();

		Map<String, Object> response = service.sendActivity(activityId, sentBy);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{activityId}")
	@Operation(summary = "Cancelar atividade", description = "Marca como 'canceled' uma atividade em 'draft'")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade cancelada"),
			@ApiResponse(responseCode = "400", description = "Atividade não está em draft"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Integer activityId) {

		service.cancelDraftActivity(activityId);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Atividade cancelada com sucesso!");
		return ResponseEntity.ok(response);
	}

}
