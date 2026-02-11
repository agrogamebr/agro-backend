package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.ActivityDecisionRequestDTO;
import br.com.agrogame.agrogame.dto.ActivityDecisionResponseDTO;
import br.com.agrogame.agrogame.dto.BackofficeActivityListDTO;
import br.com.agrogame.agrogame.dto.BackofficeSubmissionListDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.BackofficeActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/backoffice/activities")
@Tag(name = "Backoffice Activities", description = "Endpoints para aprovação de atividades no backoffice")
public class BackofficeActivityController {

	private final BackofficeActivityService backofficeActivityService;
	private final UserRepository userRepository;

	public BackofficeActivityController(BackofficeActivityService activityApprovalService,
			UserRepository userRepository) {
		this.backofficeActivityService = activityApprovalService;
		this.userRepository = userRepository;
	}

	@Operation(summary = "Listar atividades submetidas para aprovação", description = """
			    Retorna lista de atividades em status 'submitted' que aguardam aprovação pelo backoffice.

			    Acesso restrito a:
			    - user_type 1 (Manager)
			    - user_type 2 (Administrator)
			    - user_type 3 (Employee)
			    - user_type 6 (Auditor)

			    Mostra apenas atividades da empresa do usuário autenticado.
			    Inclui informações do produtor, fazenda, data de submissão e lista de arquivos.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão (não é backoffice ou não pertence à empresa)"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao listar atividades") })
	@GetMapping("/submissions")
	public ResponseEntity<Map<String, Object>> listSubmittedActivities(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Principal principal) {

		User backofficeUser = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Integer backofficeUserId = backofficeUser.getId();

		Page<BackofficeSubmissionListDTO> submissionsPage = backofficeActivityService
				.listSubmittedActivities(backofficeUserId, page, size);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("submissions", submissionsPage.getContent());
		response.put("page", submissionsPage.getNumber());
		response.put("size", submissionsPage.getSize());
		
		response.put("totalElements", submissionsPage.getTotalElements());
		response.put("totalPages", submissionsPage.getTotalPages());
		response.put("message", submissionsPage.isEmpty() ? "Nenhuma atividade aguardando aprovação"
				: "Atividades carregadas com sucesso");

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Aprovar ou rejeitar atividade submetida", description = """
			Processa a decisão do backoffice sobre uma atividade em status 'submitted'.

			Request body:
			{
			  "decision": "approved" | "rejected",   // case-insensitive
			  "reason": "texto opcional (obrigatório quando rejected)"
			}

			Regras:
			- decision: obrigatório, aceita 'approved' ou 'rejected' (qualquer combinação de maiúsculas/minúsculas).
			- reason: recomendado quando decision = 'rejected'; ignorado na aprovação.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Decisão processada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Requisição inválida"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Recurso não encontrado"),
			@ApiResponse(responseCode = "422", description = "Status inválido"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@PostMapping("/submissions/{userActivityId}/decision")
	public ResponseEntity<Map<String, Object>> makeDecision(
			@Parameter(description = "ID da UserActivity", example = "1") @PathVariable Integer userActivityId,
			@Valid @RequestBody ActivityDecisionRequestDTO decisionRequest, Principal principal) {

		try {
			// 1. Buscar usuário autenticado
			User backofficeUser = userRepository.findByEmail1(principal.getName())
					.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

			// 2. Processar decisão
			ActivityDecisionResponseDTO result = backofficeActivityService.makeDecision(userActivityId,
					backofficeUser.getId(), decisionRequest);

			// 3. Montar resposta
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("result", result);
			response.put("message", "approved".equals(result.getDecision()) ? "Atividade aprovada com sucesso!"
					: "Atividade rejeitada com sucesso!");

			return ResponseEntity.ok(response);

		} catch (ResourceNotFoundException e) {
			return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (AccessDeniedException e) {
			return buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
		} catch (BusinessException e) {
			return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
		} catch (Exception e) {
			return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("success", false);
		errorResponse.put("message", message);
		return ResponseEntity.status(status).body(errorResponse);
	}

	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> listCompanyActivities(@RequestParam(required = false) String status,
			@RequestParam(required = false) String submissionStatus, @RequestParam(required = false) Integer cropTypeId,
			@RequestParam(required = false) Integer farmId, @RequestParam(required = false) Integer productionUnitId,
			@RequestParam(required = false) Integer producerId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Principal principal) {

		User backofficeUser = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Integer companyId = backofficeUser.getCompany().getId();

		Page<BackofficeActivityListDTO> activitiesPage = backofficeActivityService.listActivitiesForBackoffice(
				companyId, status, submissionStatus, producerId, farmId, productionUnitId, cropTypeId, startDate,
				endDate, page, size);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activitiesPage.getContent());
		response.put("page", activitiesPage.getNumber());
		response.put("size", activitiesPage.getSize());
		response.put("totalElements", activitiesPage.getTotalElements());
		response.put("totalPages", activitiesPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

}
