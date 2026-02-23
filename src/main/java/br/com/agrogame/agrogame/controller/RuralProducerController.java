package br.com.agrogame.agrogame.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	public ResponseEntity<Map<String, Object>> listProducerActivities(@RequestParam(required = false) Integer farmId,
			@RequestParam(required = false) Integer cropTypeId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFromStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFromEnd,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validToStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validToEnd,
			@RequestParam(required = false) String name, 
			@RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		Page<ProducerActivityDTO> activitiesPage = ruralProducerService.listActivitiesForProducerFast(producerId,
				farmId, cropTypeId, name, validFromStart, validFromEnd, validToStart, validToEnd, status, page, size);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activitiesPage.getContent());
		response.put("page", activitiesPage.getNumber());
		response.put("size", activitiesPage.getSize());
		response.put("totalElements", activitiesPage.getTotalElements());
		response.put("totalPages", activitiesPage.getTotalPages());

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
	@PostMapping(value = "/user-activities/{userActivityId}/files", consumes = "multipart/form-data")
	public ResponseEntity<Map<String, Object>> uploadFile(
			@Parameter(description = "ID da UserActivity", example = "1") @PathVariable Integer userActivityId,

			@Parameter(description = "Arquivo para anexar") @RequestPart("file") MultipartFile file,

			@Parameter(description = "Descrição ou comentário sobre o arquivo", example = "Foto da atividade realizada") @RequestPart("description") String description,

			Principal principal) throws IOException {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado")).getId();

		FileUploadResponseDTO responseDto = activitySubmissionService.uploadFile(producerId, userActivityId, file,
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
	@PostMapping("/user-activities/{userActivityId}/submit")
	public ResponseEntity<Map<String, Object>> submitActivity(
			@Parameter(description = "ID da UserActivity", example = "1") @PathVariable Integer userActivityId,
			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		SubmitActivityResponseDTO responseDto = activitySubmissionService.submitActivity(producerId, userActivityId);

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
	@GetMapping("/user-activities/{userActivityId}/files/{submissionFileId}/download")
	public ResponseEntity<byte[]> downloadSubmissionFile(
			@Parameter(description = "ID da UserActivity", example = "6") @PathVariable Integer userActivityId,

			@Parameter(description = "ID do arquivo de submissão", example = "10") @PathVariable Integer submissionFileId,

			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		return activitySubmissionService.downloadSubmissionFile(producerId, userActivityId, submissionFileId);
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
			Retorna a lista paginada de transações de pontos do produtor logado.
			Ordenação padrão: Mais recentes primeiro.
			""")
	@GetMapping("/points/transactions")
	public ResponseEntity<Page<ProducerPointsTransactionDTO>> getTransactions(
			@Parameter(description = "ID da fazenda para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer farmId,

			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,

			Principal principal) {

		User user = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		// Validação de segurança da fazenda
		if (farmId != null) {
			Farm farm = farmRepository.findById(farmId)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(user.getId())) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
		}

		// Cria o Pageable ordenando por data de criação (descendente - mais novo
		// primeiro)
		// Assumindo que o campo de data na entidade Transaction seja 'createdAt'
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<ProducerPointsTransactionDTO> transactions = producerPointsService.getTransactions(user.getId(), farmId,
				pageable);

		return ResponseEntity.ok(transactions);
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
