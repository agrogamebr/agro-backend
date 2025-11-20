package br.com.agrogame.agrogame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.ErrorResponseDTO;
import br.com.agrogame.agrogame.dto.LoginRequestDTO;
import br.com.agrogame.agrogame.dto.LoginResponseDTO;
import br.com.agrogame.agrogame.exceptions.AuthenticationException;
import br.com.agrogame.agrogame.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Login",
        description = "Autentica usuário com email, CPF ou CNPJ e retorna JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
        @ApiResponse(responseCode = "400", description = "Identificador ou formato inválido"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta bloqueada"),
        @ApiResponse(responseCode = "403", description = "Produtor pendente/rejeitado ou usuário inativo"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            LoginResponseDTO response = authService.login(dto);
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            int status = getStatusFromErrorCode(e.getErrorCode());
            return ResponseEntity
                .status(status)
                .body(new ErrorResponseDTO(e.getErrorCode(), e.getMessage(), status));
                
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(
                    "INTERNAL_ERROR", 
                    "Erro ao processar login. Tente novamente.", 
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
        }
    }

    private int getStatusFromErrorCode(String errorCode) {
        return switch(errorCode) {
            case "INVALID_CREDENTIALS", "ACCOUNT_LOCKED" -> 401;
            case "PRODUCER_PENDING", "PRODUCER_REJECTED", "USER_INACTIVE" -> 403;
            case "INVALID_IDENTIFIER" -> 400;
            default -> 500;
        };
    }
}


