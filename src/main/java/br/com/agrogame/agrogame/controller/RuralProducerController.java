package br.com.agrogame.agrogame.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.CompanyListDTO;
import br.com.agrogame.agrogame.dto.FileUploadResponseDTO;
import br.com.agrogame.agrogame.dto.ProducerActivityDTO;
import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
import br.com.agrogame.agrogame.dto.ProducerPointsTransactionDTO;
import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.dto.SubmitActivityResponseDTO;
import br.com.agrogame.agrogame.dto.UserActivityDetailDTO;
import br.com.agrogame.agrogame.dto.UserDocumentTypeDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.ActivitySubmissionService;
import br.com.agrogame.agrogame.service.CompanyService;
import br.com.agrogame.agrogame.service.ProducerPointsService;
import br.com.agrogame.agrogame.service.RuralProducerService;
import br.com.agrogame.agrogame.service.UserActivityService;
import br.com.agrogame.agrogame.service.UserDocumentTypeService;
import br.com.agrogame.agrogame.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/producer")
@Tag(name = "Rural Producer", description = "Cadastro de produtores rurais")
public class RuralProducerController {

	@Autowired
	private RuralProducerService ruralProducerService;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private UserDocumentTypeService userDocumentTypeService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProducerPointsService producerPointsService;

	@Autowired
	private ActivitySubmissionService activitySubmissionService;

	@Autowired
	private UserActivityService userActivityService;
	
	@Autowired
	private FarmRepository farmRepository;

	@Operation(summary = "Cadastrar produtor rural", description = """
			  Cria novo produtor vinculado a uma empresa parceira.
			  Principais erros:
			  - 400: Dados inválidos (formato obrigatório, campos ausentes)
			  - 404: Empresa ou tipo de documento não encontrado
			  - 409: E-mail ou documento já cadastrado para outro usuário
			  - 422: Regra de negócio impedindo cadastro (ex: empresa não ativa para receber produtores)
			  - 500: Erro inesperado no servidor
			""")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Produtor cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos (ex: formato de email)"),
			@ApiResponse(responseCode = "404", description = "Empresa ou tipo de documento não encontrado"),
			@ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado"),
			@ApiResponse(responseCode = "422", description = "Regras de negócio não atendidas"),
			@ApiResponse(responseCode = "500", description = "Erro inesperado no servidor") })
	@PostMapping("/register")
	public ResponseEntity<?> registerProducer(@Valid @RequestBody RuralProducerDTO dto) {
		User producer = ruralProducerService.registerRuralProducer(dto);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Cadastro realizado com sucesso! Aguarde a aprovação da empresa parceira.");
		response.put("producerId", producer.getId());
		response.put("producerName", producer.getFullName());
		response.put("status", producer.getUserStatus().getName());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Validar Email em tempo real", description = "Verifica se o email já está cadastrado em empresas ou usuários.")
	@ApiResponse(responseCode = "200", description = "Retorna {'valid': true/false, 'message': '...'}")
	@GetMapping("/validate-email")
	public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
		Map<String, Object> response = new HashMap<>();

		if (!validationService.isValidEmailFormat(email)) {
			response.put("valid", false);
			response.put("message", "Formato de e-mail inválido");
			return ResponseEntity.badRequest().body(response);
		}

		boolean alreadyExists = validationService.emailAlreadyExists(email);
		response.put("valid", !alreadyExists);
		response.put("message", alreadyExists ? "E-mail já cadastrado" : "E-mail disponível");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Validar documento em tempo real", description = "Verifica se o documento (CPF, RG, CNH, Passaporte) informado já está cadastrado para outro usuário.")
	@ApiResponse(responseCode = "200", description = "Retorna {'valid': true/false, 'message': '...'}")
	@GetMapping("/validate-document")
	public ResponseEntity<Map<String, Object>> validateDocument(@RequestParam Integer documentTypeId,
			@RequestParam String documentNumber) {
		Map<String, Object> response = new HashMap<>();

		if (documentNumber == null || documentNumber.isBlank()) {
			response.put("valid", false);
			response.put("message", "Número do documento não informado");
			return ResponseEntity.badRequest().body(response);
		}

		boolean exists = validationService.documentAlreadyExists(documentTypeId, documentNumber);
		response.put("valid", !exists);
		response.put("message", exists ? "Documento já cadastrado" : "Documento disponível");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Listar tipos de documento", description = "Obtém todos os tipos de documento cadastrados. Use status='active' para apenas ativos, status='inactive' para inativos ou deixe em branco para todos.")
	@GetMapping("/document-types")
	public ResponseEntity<Map<String, Object>> listAllDocumentTypes(
			@Parameter(description = "Status do tipo de documento: 'active' para ativos, 'inactive' para inativos. Se em branco, retorna todos.", example = "active") @RequestParam(required = false) String status) {
		List<UserDocumentType> entities = userDocumentTypeService.listAllDocumentTypes(status);
		List<UserDocumentTypeDTO> dtos = entities.stream().map(UserDocumentTypeDTO::new).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("items", dtos);
		response.put("count", dtos.size());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/companies/active")
	public List<CompanyListDTO> findAllActive() {
		return companyService.findAllActive().stream().map(CompanyListDTO::new).toList();
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

	@Operation(summary = "Listar atividades disponíveis para produtor", description = """
			  Retorna as atividades cadastradas pela empresa do produtor,
			  filtradas por fazenda, cultura, datas de validade e descrição.
			  Apenas atividades em status 'send' são exibidas.

			  Filtros opcionais:
			  - farmId: filtrar por fazenda específica
			  - cropTypeId: filtrar por cultura específica
			  - validFromStart / validFromEnd: filtrar por data de início (formato: yyyy-MM-dd)
			  - validToStart / validToEnd: filtrar por data de término (formato: yyyy-MM-dd)
			  - nome: filtrar por trecho da descrição da atividade (contains, case-insensitive)
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de atividades agrupadas por fazenda"),
			@ApiResponse(responseCode = "400", description = "Produtor ou fazenda inválido"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Usuário não é produtor rural"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@GetMapping("/activities")
	public ResponseEntity<Map<String, Object>> listProducerActivities(
			@Parameter(description = "ID da fazenda para filtrar", example = "1") @RequestParam(required = false) Integer farmId,

			@Parameter(description = "ID do tipo de cultura para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer cropTypeId,

			@Parameter(description = "Data inicial de validade (formato: yyyy-MM-dd, opcional)", example = "2025-11-26") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFromStart,

			@Parameter(description = "Data final de validade (formato: yyyy-MM-dd, opcional)", example = "2025-12-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFromEnd,

			@Parameter(description = "Data inicial de término (formato: yyyy-MM-dd, opcional)", example = "2025-11-26") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validToStart,

			@Parameter(description = "Data final de término (formato: yyyy-MM-dd, opcional)", example = "2025-11-30") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validToEnd,

			@Parameter(description = "Trecho do nome da atividade para filtrar (opcional)", example = "Plantio Soja") @RequestParam(required = false) String name,

			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		List<ProducerActivityDTO> activities = ruralProducerService.listActivitiesForProducerFast(producerId, farmId,
				cropTypeId, name, validFromStart, validFromEnd, validToStart, validToEnd, null);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activities);
		response.put("total", activities.size());
		response.put("message", "Atividades carregadas com sucesso");

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Upload de arquivo para atividade", description = """
			  Permite que o produtor anexe arquivos (fotos, documentos) a uma atividade.
			  Arquivos permitidos: PDF, DOCX, PNG, JPEG, JPG.
			  Múltiplos uploads são permitidos enquanto a atividade estiver em status 'pending'.

			""")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Arquivo enviado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Arquivo inválido ou vazio"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para esta atividade/fazenda"),
			@ApiResponse(responseCode = "404", description = "Atividade, fazenda ou produtor não encontrado"),
			@ApiResponse(responseCode = "422", description = "Atividade não está em status 'send' ou já foi submetida"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao enviar arquivo") })
	@PostMapping(value = "/activities/{activityId}/farms/{farmId}/files", consumes = "multipart/form-data")
	public ResponseEntity<Map<String, Object>> uploadFile(
			@Parameter(description = "ID da atividade", example = "1") @PathVariable Integer activityId,

			@Parameter(description = "ID da fazenda", example = "1") @PathVariable Integer farmId,

			@Parameter(description = "Arquivo para anexar") @RequestPart("file") MultipartFile file,

			@Parameter(description = "Descrição ou comentário sobre o arquivo", example = "Foto da atividade realizada") @RequestPart("description") String description,

			Principal principal) throws IOException {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado")).getId();

		FileUploadResponseDTO responseDto = activitySubmissionService.uploadFile(producerId, activityId, farmId, file,
				description);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("submission", responseDto);
		response.put("message", "Arquivo enviado com sucesso!");

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ============ API 2: SUBMETER ATIVIDADE ============

	@Operation(summary = "Submeter atividade para aprovação", description = """
			  Finaliza a submissão de uma atividade, mudando seu status de 'pending' para 'submitted'.

			  Pré-requisitos:
			  - Atividade deve estar em status 'pending' (estado inicial).
			  - Pelo menos um arquivo deve ter sido enviado anteriormente.

			  Após a submissão:
			  - Nenhum arquivo adicional pode ser enviado para esta atividade.
			  - A atividade fica aguardando aprovação pelo backoffice.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Atividade submetida com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para esta atividade/fazenda"),
			@ApiResponse(responseCode = "404", description = "Atividade, fazenda ou produtor não encontrado"),
			@ApiResponse(responseCode = "422", description = "Atividade já foi submetida ou nenhum arquivo foi enviado"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao submeter atividade") })
	@PostMapping("/activities/{activityId}/farms/{farmId}/submit")
	public ResponseEntity<Map<String, Object>> submitActivity(
			@Parameter(description = "ID da atividade", example = "1") @PathVariable Integer activityId,

			@Parameter(description = "ID da fazenda", example = "1") @PathVariable Integer farmId,

			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		SubmitActivityResponseDTO responseDto = activitySubmissionService.submitActivity(producerId, activityId,
				farmId);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("submission", responseDto);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Download de arquivo enviado na submissão", description = """
			  Permite que o produtor baixe um arquivo que ele já enviou em uma submissão
			  de atividade para uma fazenda específica.

			  Respeita o vínculo entre produtor, atividade, fazenda e submissão.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para acessar este arquivo"),
			@ApiResponse(responseCode = "404", description = "Atividade, fazenda, submissão ou arquivo não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno ao baixar arquivo") })
	@GetMapping("/activities/{activityId}/farms/{farmId}/files/{submissionId}/download")
	public ResponseEntity<byte[]> downloadSubmissionFile(
			@Parameter(description = "ID da atividade", example = "6") @PathVariable Integer activityId,

			@Parameter(description = "ID da fazenda", example = "1") @PathVariable Integer farmId,

			@Parameter(description = "ID do registro de submissão (user_activity_submission)", example = "10") @PathVariable Integer submissionId,

			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		return activitySubmissionService.downloadSubmissionFile(producerId, activityId, farmId, submissionId);
	}

	@Operation(summary = "Obter saldo de pontos do produtor", description = """
			Retorna o saldo atual de pontos do produtor logado, considerando todas as transações de pontos.

			Filtro opcional:
			- farmId: filtrar saldo apenas para uma fazenda específica

			Exemplo de resposta:
			{
			  "userId": 15,
			  "userName": "João da Silva",
			  "currentBalance": 1250
			}
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Saldo retornado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Fazenda não pertence ao produtor") })
	@GetMapping("/points/balance")
	public ResponseEntity<ProducerPointsBalanceDTO> getBalance(
			@Parameter(description = "ID da fazenda para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer farmId,
			Principal principal) {
		User user = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		// Validar farmId se informado
		if (farmId != null) {
			Farm farm = farmRepository.findById(farmId)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(user.getId())) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
		}

		ProducerPointsBalanceDTO dto = producerPointsService.getCurrentBalance(user.getId(), farmId);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Listar histórico de pontos do produtor", description = """
			Retorna a lista de transações de pontos do produtor logado, ordenadas da mais recente para a mais antiga.

			Filtro opcional:
			- farmId: filtrar transações apenas para uma fazenda específica

			Cada item contém:
			- tipo de transação (earn, spend, adjust)
			- fonte (ex: activity_approval)
			- descrição da atividade (se houver)
			- nome da recompensa (se houver)
			- pontos da transação
			- saldo após a transação
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Fazenda não pertence ao produtor") })
	@GetMapping("/points/transactions")
	public ResponseEntity<Map<String, Object>> getTransactions(
			@Parameter(description = "ID da fazenda para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer farmId,
			Principal principal) {
		User user = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		// Validar farmId se informado
		if (farmId != null) {
			Farm farm = farmRepository.findById(farmId)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(user.getId())) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
		}

		List<ProducerPointsTransactionDTO> transactions = producerPointsService.getTransactions(user.getId(), farmId);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("items", transactions);
		response.put("total", transactions.size());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Detalhar atividade do produtor", description = """
			Retorna os dados da atividade vinculada ao produtor,
			com base no ID de UserActivity.

			Regras:
			- Apenas o próprio produtor pode consultar suas atividades.
			- A atividade deve pertencer à mesma empresa/regra de negócio configurada.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Detalhamento da atividade retornado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou atividade não pertence ao produtor"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Usuário não é produtor rural"),
			@ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@GetMapping("/user-activities/{userActivityId}")
	public ResponseEntity<UserActivityDetailDTO> getUserActivityDetail(
			@Parameter(description = "ID da UserActivity para detalhar", example = "1") @PathVariable Integer userActivityId,
			Principal principal) {
		UserActivityDetailDTO detail = userActivityService.getUserActivityDetail(principal.getName(), userActivityId);

		return ResponseEntity.ok(detail);
	}

}
