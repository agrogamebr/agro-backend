package br.com.agrogame.agrogame.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.RuralProducerDTO;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.service.RuralProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/producer")
@Tag(name = "Rural Producer", description = "Cadastro de produtores rurais")
public class RuralProducerController {
    
    @Autowired
    private RuralProducerService ruralProducerService;
    
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
}
