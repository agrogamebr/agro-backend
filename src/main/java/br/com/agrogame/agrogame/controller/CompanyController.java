package br.com.agrogame.agrogame.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.CompanyDocumentDTO;
import br.com.agrogame.agrogame.dto.CompanyListDTO;
import br.com.agrogame.agrogame.dto.CreateCompanyDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyDocumentType;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/company")
@Tag(name = "Company", description = "Endpoints para cadastro de empresas")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar empresa", description = "Cria uma nova empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Empresa cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação dos dados")
    })
    @PostMapping("/createCompany")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CreateCompanyDTO dto) {
        try {
            // Pega CNPJ da lista de documentos enviada no DTO
            String cnpj = dto.getDocumentos().stream()
                .filter(doc -> doc.getType() == EnumCompanyDocumentType.CNPJ)
                .map(CompanyDocumentDTO::getDocumentNumber)
                .findFirst()
                .orElse(null);

            // Toda lógica de conversão e persistência está na service
            Company saved = service.registerCompany(dto);

            // TODO: enviar e-mail de boas-vindas
            // TODO: notificar dono do Agro App

            return ResponseEntity.status(201)
                .body("Cadastro realizado com sucesso! Sua solicitação está pendente de aprovação.");
        } catch (Exception e) {
        	 e.printStackTrace();  // Imprime stacktrace completo
             System.err.println("ERRO: " + e.getMessage());
             e.getCause(); // Se houver causa raiz
             return ResponseEntity.status(400)
                 .body("Erro ao cadastrar empresa: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar todas empresas", description = "Retorna todas as empresas cadastradas")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")})
    @GetMapping("/companies")
    public List<CompanyListDTO> findAll() {
        return service.findAll().stream()
            .map(CompanyListDTO::new)
            .toList();
    }


}
