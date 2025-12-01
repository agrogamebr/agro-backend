package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.CompanyListDTO;
import br.com.agrogame.agrogame.dto.ProducerActivityByFarmDTO;
import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.dto.UserDocumentTypeDTO;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.CompanyService;
import br.com.agrogame.agrogame.service.RuralProducerService;
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
			  filtradas por fazenda e cultura compatíveis com suas plantações.
			  Apenas atividades em status 'send' são exibidas.

			  Filtros opcionais:
			  - farmId: filtrar por fazenda específica
			  - cropTypeId: filtrar por cultura específica
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de atividades agrupadas por fazenda"),
			@ApiResponse(responseCode = "400", description = "Produtor ou fazenda inválido"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "403", description = "Usuário não é produtor rural"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@GetMapping("/activities")
	public ResponseEntity<Map<String, Object>> listProducerActivities(
			@Parameter(description = "ID da fazenda para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer farmId,

			@Parameter(description = "ID do tipo de cultura para filtrar (opcional)", example = "1") @RequestParam(required = false) Integer cropTypeId,

			Principal principal) {

		Integer producerId = userRepository.findByEmail1(principal.getName())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();

		List<ProducerActivityByFarmDTO> activities = ruralProducerService.listActivitiesForProducer(producerId, farmId,
				cropTypeId);

		Map<String, Object> response = new HashMap<>();
		response.put("activities", activities);
		response.put("total", activities.size());
		response.put("message", "Atividades carregadas com sucesso");

		return ResponseEntity.ok(response);
	}

}
