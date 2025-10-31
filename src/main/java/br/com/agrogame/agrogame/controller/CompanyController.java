package br.com.agrogame.agrogame.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.CompanyDocumentDTO;
import br.com.agrogame.agrogame.dto.CompanyListDTO;
import br.com.agrogame.agrogame.dto.CompanyTypeDTO;
import br.com.agrogame.agrogame.dto.CreateCompanyDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyDocumentType;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.service.CompanyService;
import br.com.agrogame.agrogame.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/company")
@Tag(name = "Company", description = "Endpoints para cadastro e validação de empresas")
public class CompanyController {

	private final CompanyService service;
	private final ValidationService validationService;

	public CompanyController(CompanyService service, ValidationService validationService) {
		this.service = service;
		this.validationService = validationService;
	}

	@Operation(summary = "Cadastrar empresa", description = "Cria uma nova empresa com validações obrigatórias")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Empresa cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Erro de validação dos dados"),
			@ApiResponse(responseCode = "409", description = "CNPJ ou email já cadastrado"),
			@ApiResponse(responseCode = "500", description = "Erro ao processar cadastro")
	})
	@PostMapping("/create-company")
	public ResponseEntity<?> createCompany(@Valid @RequestBody CreateCompanyDTO dto) {
		try {
			// 1. Extrair CNPJ dos documentos
			String cnpj = dto.getDocumentos().stream()
					.filter(doc -> doc.getDocument() == EnumCompanyDocumentType.CNPJ)
					.map(CompanyDocumentDTO::getDocumentNumber)
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("CNPJ é obrigatório na lista de documentos"));

			// 2. Registrar empresa COM usuário padrão e senha informada
			Company saved = service.registerCompany(dto, cnpj);

			// 3. Retornar sucesso
			Map<String, Object> response = new HashMap<>();
			response.put("id", saved.getId());
			response.put("companyName", saved.getFullCompanyName());
			response.put("email", saved.getEmail1());
			response.put("status", saved.getCompanyStatus().getCode());
			response.put("message", "Cadastro realizado com sucesso! Sua solicitação está pendente de aprovação.");

			return ResponseEntity.status(201).body(response);

		} catch (IllegalArgumentException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(400).body(error);

		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> error = new HashMap<>();
			error.put("error", "Houve um erro ao processar seu cadastro. Por favor, tente novamente.");
			return ResponseEntity.status(500).body(error);
		}
	}


	@Operation(summary = "Validar CNPJ em tempo real")
	@ApiResponse(responseCode = "200", description = "Retorna {'valid': true/false, 'message': '...'}")
	@GetMapping("/validate-cnpj")
	public ResponseEntity<Map<String, Object>> validateCnpj(@RequestParam String cnpj) {
		Map<String, Object> response = new HashMap<>();

		if (!validationService.isValidCnpjFormat(cnpj)) {
			response.put("valid", false);
			response.put("message", "Formato de CNPJ inválido");
			return ResponseEntity.badRequest().body(response);
		}

		boolean alreadyExists = validationService.cnpjAlreadyExists(cnpj);
		response.put("valid", !alreadyExists);
		response.put("message", alreadyExists ? "CNPJ já cadastrado" : "CNPJ disponível");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Validar Email em tempo real")
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

	@Operation(summary = "Listar todas empresas", description = "Retorna todas as empresas cadastradas")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")})
	@GetMapping("/companies")
	public List<CompanyListDTO> findAll() {
		return service.findAll().stream()
				.map(CompanyListDTO::new)
				.toList();
	}

	@Operation(summary = "Listar todos os company types", description = "Retorna todos os tipos de empresa cadastrados")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")})
	@GetMapping("/company-types")
	public List<CompanyTypeDTO> findAllCompanyTypes(){
		return service.findAllCompanyTypes().stream()
				.map(CompanyTypeDTO::new)
				.toList();
	}

	@Operation(
			summary = "Aprovar empresa", 
			description = "Altera o status da empresa de PENDING para APPROVED. **Apenas usuários com perfil MANAGER podem aprovar.**"
			)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Empresa aprovada com sucesso",
					content = @Content(mediaType = "application/json",
					examples = @ExampleObject(value = "{\"id\": 1, \"companyName\": \"Agroinova\", \"status\": \"approved\", \"message\": \"Empresa aprovada com sucesso!\"}")
							)
					),
			@ApiResponse(responseCode = "400", description = "Empresa não está com status PENDING",
			content = @Content(mediaType = "application/json",
			examples = @ExampleObject(value = "{\"error\": \"Somente empresas com status PENDING podem ser aprovadas\"}")
					)
					),
			@ApiResponse(responseCode = "403", description = "Acesso negado - apenas MANAGER pode aprovar",
			content = @Content(mediaType = "application/json",
			examples = @ExampleObject(value = "{\"error\": \"Acesso negado\"}")
					)
					),
			@ApiResponse(responseCode = "404", description = "Empresa não encontrada")
	})
	@PatchMapping("/approve/{companyId}")
	public ResponseEntity<?> approveCompany(@PathVariable Integer companyId) {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
	        
			Company approved = service.approveCompany(companyId, userEmail);

			Map<String, Object> response = new HashMap<>();
			response.put("id", approved.getId());
			response.put("companyName", approved.getFullCompanyName());
			response.put("status", approved.getCompanyStatus().getCode());
			response.put("message", "Empresa aprovada com sucesso!");

			return ResponseEntity.ok(response);

		} catch (IllegalStateException e) {
			// Status inválido para aprovação
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(400).body(error);

		} catch (RuntimeException e) {
			// Empresa não encontrada ou erro de status
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(404).body(error);

		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> error = new HashMap<>();
			error.put("error", "Erro ao aprovar empresa. Tente novamente.");
			return ResponseEntity.status(500).body(error);
		}
	}

}
