package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.ActivityListDTO;
import br.com.agrogame.agrogame.dto.CreateActivityDTO;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityReward;
import br.com.agrogame.agrogame.repository.ActivityCropTypeRepository;
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
	private final ActivityRepository activityRepository;
	private final ActivityRewardRepository activityRewardRepository;
	private final ActivityCropTypeRepository activityCropTypeRepository;
	private final RewardRepository rewardRepository;

	public ActivityController(ActivityService service, UserRepository userRepository,
			ActivityRewardRepository activityRewardRepository, ActivityRepository activityRepository,
			ActivityCropTypeRepository activityCropTypeRepository, RewardRepository rewardRepository) {
		this.service = service;
		this.userRepository = userRepository;
		this.activityRepository = activityRepository;
		this.activityRewardRepository = activityRewardRepository;
		this.activityCropTypeRepository = activityCropTypeRepository;
		this.rewardRepository = rewardRepository;
	}

	@Operation(summary = "Listar atividades da empresa", description = "Retorna a lista de atividades da empresa do usuário logado. "
			+ "Opcionalmente filtra por status (ex: draft, active, send, expired, inactive, completed, canceled). ")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao listar atividades") })
	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> listActivities(@RequestParam(required = false) String status,
			Principal principal) {

		Integer companyId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getCompany().getId();

		List<ActivityListDTO> activities = service.listActivities(companyId, status);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activities);
		response.put("total", activities.size());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/create-activity")
	public ResponseEntity<Map<String, Object>> createActivity(@Valid @RequestBody CreateActivityDTO dto,
			Principal principal) {

		Integer createdBy = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		Map<String, Object> response = service.registerActivity(dto, createdBy);
		return ResponseEntity.status(201).body(response);
	}

	@GetMapping("/{activityId}")
	@Operation(summary = "Buscar atividade", description = "Retorna os detalhes de uma atividade para edição")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade encontrada"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	public ResponseEntity<Map<String, Object>> getActivity(@PathVariable Integer activityId) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));

		Map<String, Object> response = new HashMap<>();
		response.put("id", activity.getId());
		response.put("companyId", activity.getCompany().getId());
		response.put("description", activity.getDescription());
		response.put("points", activity.getPoints());
		response.put("status", activity.getActivityStatus().getCode());
		response.put("validFrom", activity.getValidFrom());
		response.put("validTo", activity.getValidTo());
		response.put("cropTypeIds", getCropTypeIds(activity.getId()));

		return ResponseEntity.ok(response);
	}

	private List<Integer> getCropTypeIds(Integer activityId) {
		return activityCropTypeRepository.findByActivityId(activityId).stream().map(act -> act.getCropType().getId())
				.toList();
	}

	@PutMapping("/{activityId}")
	@Operation(summary = "Editar atividade", description = "Edita uma atividade que está em status 'draft'")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade atualizada"),
			@ApiResponse(responseCode = "400", description = "Atividade não está em draft"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	public ResponseEntity<Map<String, Object>> updateActivity(@PathVariable Integer activityId,
			@Valid @RequestBody CreateActivityDTO dto, Principal principal) {

		Integer updatedBy = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		Map<String, Object> response = service.updateActivity(activityId, dto, updatedBy);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{activityId}/send")
	@Operation(
		    summary = "Enviar atividade",
		    description = "Altera o status de uma atividade de 'draft' para 'send', "
		                + "bloqueando novas edições e disparando o fluxo de envio para os producers elegíveis."
		)
		@ApiResponses({
		    @ApiResponse(responseCode = "200", description = "Atividade enviada com sucesso"),
		    @ApiResponse(responseCode = "400", description = "Apenas atividades em status 'draft' podem ser enviadas"),
		    @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
		    @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
		    @ApiResponse(responseCode = "500", description = "Erro interno ao enviar atividade")
		})
	public ResponseEntity<Map<String, Object>> sendActivity(@PathVariable Integer activityId, Principal principal) {

		Integer sentBy = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		Map<String, Object> response = service.sendActivity(activityId, sentBy);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{activityId}")
	@Operation(summary = "Deletar atividade", description = "Deleta uma atividade que está em status 'draft'")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade deletada"),
			@ApiResponse(responseCode = "400", description = "Atividade não está em draft"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Integer activityId) {

		Activity activity = activityRepository.findById(activityId)
				.orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));

		if (!"draft".equals(activity.getActivityStatus().getCode())) {
			throw new IllegalArgumentException("Apenas atividades em draft podem ser deletadas");
		}

		// Deletar crop types vinculados
		activityCropTypeRepository.deleteByActivityId(activityId);

		// Deletar activity rewards
		activityRewardRepository.deleteByActivityId(activityId);

		// Deletar reward se foi criado automaticamente
		List<ActivityReward> rewards = activityRewardRepository.findByActivityId(activityId);
		for (ActivityReward ar : rewards) {
			rewardRepository.deleteById(ar.getReward().getId());
		}

		// Deletar activity
		activityRepository.deleteById(activityId);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Atividade deletada com sucesso!");

		return ResponseEntity.ok(response);
	}

}
