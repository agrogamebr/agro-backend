package br.com.agrogame.agrogame.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.service.RuralProducerService;
import br.com.agrogame.agrogame.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    
    @Operation(summary = "Cadastrar produtor rural", description = "Cria novo produtor vinculado a uma empresa parceira")
    @PostMapping("/register")
    public ResponseEntity<?> registerProducer(@Valid @RequestBody RuralProducerDTO dto) {
        try {
            User producer = ruralProducerService.registerRuralProducer(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cadastro realizado com sucesso! Aguarde a aprovação da empresa parceira.");
            response.put("producerId", producer.getId());
            response.put("producerName", producer.getFullName());
            response.put("status", producer.getUserStatus().getName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao processar cadastro. Tente novamente mais tarde.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
    
    @Operation(
            summary = "Validar documento em tempo real",
            description = "Verifica se o documento (CPF, RG, CNH, Passaporte) informado já está cadastrado para outro usuário."
        )
        @ApiResponse(responseCode = "200", description = "Retorna {'valid': true/false, 'message': '...'}")
        @GetMapping("/validate-document")
        public ResponseEntity<Map<String, Object>> validateDocument(
                @RequestParam Integer documentTypeId,
                @RequestParam String documentNumber
        ) {
            Map<String, Object> response = new HashMap<>();

            // (Opcional) você pode adicionar regras de validação de formato aqui
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
}
