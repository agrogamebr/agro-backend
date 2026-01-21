package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.UpdateUserProfileDTO;
import br.com.agrogame.agrogame.dto.UserProfileResponseDTO;
import br.com.agrogame.agrogame.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Perfil do Usuário", description = "Gerenciamento de perfil para qualquer tipo de usuário autenticado (Produtor, Auxiliar, etc).")
public class UserProfileController {

	@Autowired
	private UserProfileService userProfileService;

	@Operation(summary = "Obter perfil atual", description = "Retorna os dados cadastrais, foto e documento principal (CPF/CNPJ) do usuário logado.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso"),
			@ApiResponse(responseCode = "403", description = "Usuário não autenticado ou token inválido"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado no banco de dados") })
	@GetMapping
	public ResponseEntity<UserProfileResponseDTO> getProfile(Principal principal) {
		return ResponseEntity.ok(userProfileService.getProfile(principal.getName()));
	}

	@Operation(summary = "Atualizar dados cadastrais", description = "Atualiza nome, email, telefone e endereço do usuário. O documento (CPF/CNPJ) não é alterável por aqui.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
			@ApiResponse(responseCode = "400", description = "Erro de validação nos campos (ex: email inválido, telefone incorreto)"),
			@ApiResponse(responseCode = "403", description = "Usuário não autenticado") })
	@PutMapping
	public ResponseEntity<UserProfileResponseDTO> updateProfile(@RequestBody @Valid UpdateUserProfileDTO dto,
			Principal principal) {
		return ResponseEntity.ok(userProfileService.updateProfileData(principal.getName(), dto));
	}

	@Operation(summary = "Upload de foto de perfil", description = "Envia uma imagem para atualizar a foto do perfil do usuário. Suporta JPG e PNG.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Foto enviada e URL atualizada com sucesso", content = @Content(schema = @Schema(example = "{\"message\": \"Foto atualizada com sucesso\", \"profilePictureUrl\": \"https://storage.googleapis.com/...\"}"))),
			@ApiResponse(responseCode = "400", description = "Arquivo inválido ou erro no upload"),
			@ApiResponse(responseCode = "413", description = "Arquivo excede o tamanho máximo permitido") })
	@PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, String>> uploadPhoto(
			@Parameter(description = "Arquivo de imagem (jpg, png). Máx 5MB.", required = true) @RequestParam("file") MultipartFile file,
			Principal principal) {
		try {
			String url = userProfileService.updateProfilePicture(principal.getName(), file);
			return ResponseEntity.ok(Map.of("message", "Foto atualizada com sucesso", "profilePictureUrl", url));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", "Erro: " + e.getMessage()));
		}
	}
}
