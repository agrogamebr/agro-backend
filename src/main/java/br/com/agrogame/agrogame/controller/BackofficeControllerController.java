package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.ActivityDecisionRequestDTO;
import br.com.agrogame.agrogame.dto.ActivityDecisionResponseDTO;
import br.com.agrogame.agrogame.dto.BackofficeActivityListDTO;
import br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO;
import br.com.agrogame.agrogame.dto.BackofficeFarmDTO;
import br.com.agrogame.agrogame.dto.BackofficePointsStatementDTO;
import br.com.agrogame.agrogame.dto.BackofficeProducerSummaryDTO;
import br.com.agrogame.agrogame.dto.BackofficeSubmissionListDTO;
import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.BackofficeActivityService;
import br.com.agrogame.agrogame.service.BackofficeEmployeeService;
import br.com.agrogame.agrogame.service.BackofficeFarmService;
import br.com.agrogame.agrogame.service.BackofficePointsService;
import br.com.agrogame.agrogame.service.ProductionUnitService;
import br.com.agrogame.agrogame.service.RuralProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/backoffice")
@Tag(name = "Backoffice", description = "Gestão administrativa (Atividades, Pontos, Extratos)")
public class BackofficeControllerController {

	@Autowired
	private BackofficeActivityService backofficeActivityService;

	@Autowired
	private BackofficePointsService backofficePointsService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BackofficeFarmService backofficeFarmService;

	@Autowired
	private ProductionUnitService productionUnitService;

	@Autowired
	private BackofficeEmployeeService backofficeEmployeeservice;
	
	@Autowired
	private RuralProducerService ruralProducerService;

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
	@GetMapping("/activities/submissions")
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
	@PostMapping("/activities/submissions/{userActivityId}/decision")
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

	@GetMapping("/activities/list")
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
				endDate, backofficeUser, page, size);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activitiesPage.getContent());
		response.put("page", activitiesPage.getNumber());
		response.put("size", activitiesPage.getSize());
		response.put("totalElements", activitiesPage.getTotalElements());
		response.put("totalPages", activitiesPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Extrato detalhado de pontos", description = "Retorna lista de transações e o saldo atual do produtor selecionado.")
	@GetMapping("/points/statement")
	public ResponseEntity<Map<String, Object>> getStatement(@RequestParam(required = false) Integer producerId,
			@RequestParam(required = false) Integer farmId, @RequestParam(required = false) Integer productionUnitId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String operationType, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Principal principal) {

		User operator = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado"));

		// 1. Busca o Extrato Paginado
		Page<BackofficePointsStatementDTO> pageResult = backofficePointsService.getStatement(operator, producerId,
				farmId, productionUnitId, startDate, endDate, operationType, page, size);

		// 2. Busca o Saldo Atual (para o rodapé)
		ProducerPointsBalanceDTO balanceDTO = backofficePointsService.getProducerBalance(operator, producerId, farmId);

		Map<String, Object> response = new HashMap<>();
		response.put("items", pageResult.getContent());
		response.put("page", pageResult.getNumber());
		response.put("size", pageResult.getSize());
		response.put("totalElements", pageResult.getTotalElements());
		response.put("totalPages", pageResult.getTotalPages());
		response.put("currentBalance", balanceDTO.getCurrentBalance()); // Saldo no rodapé

		return ResponseEntity.ok(response);
	}

	@GetMapping("/farms/list")
	public ResponseEntity<Page<BackofficeFarmDTO>> listFarms(Principal principal,
			@RequestParam(required = false) Integer farmId, @RequestParam(required = false) String name,
			@RequestParam(required = false) Integer ownerId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		User operator = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado"));

		Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

		Page<BackofficeFarmDTO> farms = backofficeFarmService.listFarmsByCompany(operator, farmId, name, ownerId,
				pageable);

		return ResponseEntity.ok(farms);
	}

	@GetMapping("/production-units/list")
	public ResponseEntity<Page<ProductionUnitDetailDTO>> listUnits(Principal principal,
			@RequestParam(required = false) Integer farmId, @RequestParam(required = false) Integer unitId,
			@RequestParam(required = false) String name, @RequestParam(required = false) Integer cropTypeId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		User operator = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado"));

		Pageable pageable = PageRequest.of(page, size);

		Page<ProductionUnitDetailDTO> result = productionUnitService.listBackofficeUnits(operator, farmId, unitId, name,
				cropTypeId, pageable);

		return ResponseEntity.ok(result);
	}

//	@Operation(summary = "Listar TODOS os usuários da empresa", description = "Retorna os dados detalhados do usuário dentro do jogo.")
//	@GetMapping("/user-info/list")
//	public ResponseEntity<Page<BackofficeEmployeeSummaryDTO>> listUserInfo(Principal principal,
//			@RequestParam(required = false) Integer userId, @RequestParam(required = false) String name,
//			@RequestParam(required = false) Integer userTypeId, @RequestParam(required = false) String cpf,
//			@RequestParam(required = false) Integer statusId, @RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "20") int size) {
//
//		User operator = userRepository.findByEmail1(principal.getName())
//				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
//
//		Pageable pageable = PageRequest.of(page, size);
//
//		Page<BackofficeEmployeeSummaryDTO> result = backofficeEmployeeservice.listEmployees(operator, userId, name,
//				userTypeId, cpf, statusId, pageable);
//
//		return ResponseEntity.ok(result);
//	}

	@Operation(summary = "Detalhar usuário", description = "Retorna os dados detalhados de um usuário dentro do jogo.")
	@GetMapping("/user-info/{userId}")
	public ResponseEntity<BackofficeEmployeeSummaryDTO> getUserInfo(Principal principal, @PathVariable Integer userId) {

		User operator = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		BackofficeEmployeeSummaryDTO dto = backofficeEmployeeservice.getUserInfo(operator, userId);

		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Listar os produtores vinculados a empresa", description = "Retorna os produtores que pertencem a sua empresa dentro do jogo.")
	@GetMapping("/producers/list")
	public ResponseEntity<Page<BackofficeProducerSummaryDTO>> listProducers(Principal principal,
			@RequestParam(required = false) String name, @RequestParam(required = false) String cpf,
			@RequestParam(required = false) Integer statusId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		User operator = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Pageable pageable = PageRequest.of(page, size);

		Page<BackofficeProducerSummaryDTO> result = backofficeEmployeeservice.listProducers(operator, name, cpf,
				statusId, pageable);

		return ResponseEntity.ok(result);
	}
	
	@Operation(summary = "Aprovar associação de funcionário na empresa", description = """
			  Faz a aprovação da associação de um produtor rural à empresa do usuário autenticado.
			  Somente administrador, manager ou employee da empresa do produtor podem aprovar.

			  Principais erros:
			  - 400: Regra de negócio violada (ex: status do produtor não está PENDING)
			  - 403: Acesso negado (usuário não tem autoridade para aprovar)
			  - 404: Produtor rural ou usuário autenticado não encontrado, ou não pertencem à mesma empresa
			  - 422: Regra de negócio específica não atendida (ex: já aprovado, associação não permitida)
			  - 500: Erro inesperado no servidor
			""")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Produtor aprovado/associado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Regra de negócio violada"),
			@ApiResponse(responseCode = "403", description = "Acesso negado ao recurso"),
			@ApiResponse(responseCode = "404", description = "Usuário/Produtor não encontrado ou não pertence à sua empresa"),
			@ApiResponse(responseCode = "422", description = "Regras específicas de negócio não atendidas"),
			@ApiResponse(responseCode = "500", description = "Erro inesperado no servidor") })
	@PatchMapping("/associate/{userId}")
	@PreAuthorize("hasAnyAuthority('administrator', 'manager', 'employee')")
	public ResponseEntity<?> associateProducer(@PathVariable Long userId) {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		User approved = ruralProducerService.associateProducer(userId, userEmail);

		Map<String, Object> response = new HashMap<>();
		response.put("id", approved.getId());
		response.put("userName", approved.getFullName());
		response.put("status", approved.getUserStatus().getCode());
		response.put("message", "Produtor rural associado com sucesso!");

		return ResponseEntity.ok(response);
	}

}
