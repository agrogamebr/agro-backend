package br.com.agrogame.agrogame.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.FarmCreateUpdateDTO;
import br.com.agrogame.agrogame.dto.FarmDetailDTO;
import br.com.agrogame.agrogame.service.FarmPhotoService;
import br.com.agrogame.agrogame.service.FarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/farms")
@Tag(name = "Farms", description = "API de propriedades rurais")
public class FarmController {

	@Autowired
	private FarmService farmService;

	@Autowired
	private FarmPhotoService farmPhotoService;

	@Operation(summary = "Cadastrar nova fazenda", description = "Cria uma propriedade rural vinculada ao usuário autenticado (producer).")
	@PostMapping
	public ResponseEntity<FarmDetailDTO> createFarm(@Valid @RequestBody FarmCreateUpdateDTO body, Principal principal) {

		FarmDetailDTO dto = farmService.createFarm(principal.getName(), body);
		return ResponseEntity.status(201).body(dto);
	}

	@Operation(summary = "Listar fazendas do usuário")
	@GetMapping
	public ResponseEntity<List<FarmDetailDTO>> listFarms(Principal principal) {
		List<FarmDetailDTO> farms = farmService.listFarms(principal.getName());
		return ResponseEntity.ok(farms);
	}

	@Operation(summary = "Detalhar fazenda do usuário")
	@GetMapping("/{farmId}")
	public ResponseEntity<FarmDetailDTO> getFarm(@PathVariable Integer farmId, Principal principal) {
		FarmDetailDTO dto = farmService.getFarm(principal.getName(), farmId);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Atualizar fazenda do usuário")
	@PutMapping("/{farmId}")
	public ResponseEntity<FarmDetailDTO> updateFarm(@PathVariable Integer farmId,
			@Valid @RequestBody FarmCreateUpdateDTO body, Principal principal) {
		FarmDetailDTO dto = farmService.updateFarm(principal.getName(), farmId, body);
		return ResponseEntity.ok(dto);
	}

	@DeleteMapping("/{farmId}")
	@Operation(summary = "Excluir (desativar) fazenda do usuário", description = "Desativa uma propriedade rural (soft delete). A fazenda não será deletada do banco, apenas marcada como inativa.", responses = {
			@ApiResponse(responseCode = "200", description = "Fazenda desativada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmDetailDTO.class))),
			@ApiResponse(responseCode = "404", description = "Fazenda não encontrada"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para desativar esta fazenda") })
	public ResponseEntity<FarmDetailDTO> deleteFarm(
			@Parameter(description = "ID da fazenda a desativar", example = "1") @PathVariable Integer farmId,
			Principal principal) {

		FarmDetailDTO dto = farmService.deleteFarm(principal.getName(), farmId);
		return ResponseEntity.ok(dto);
	}

	@PatchMapping("/{farmId}/reactivate")
	@Operation(summary = "Reativar fazenda do usuário", description = "Reativa uma propriedade rural que foi desativada (soft delete).", responses = {
			@ApiResponse(responseCode = "200", description = "Fazenda reativada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FarmDetailDTO.class))),
			@ApiResponse(responseCode = "404", description = "Fazenda não encontrada"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para reativar esta fazenda") })
	public ResponseEntity<FarmDetailDTO> reactivateFarm(
			@Parameter(description = "ID da fazenda a reativar", example = "1") @PathVariable Integer farmId,
			Principal principal) {

		FarmDetailDTO dto = farmService.reactivateFarm(principal.getName(), farmId);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Upload de foto da fazenda", description = "Atualiza a foto de perfil da fazenda. Retorna a URL pública.")
	@PostMapping(value = "/{farmId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, String>> uploadPhoto(@PathVariable Integer farmId,
			@RequestPart("file") MultipartFile file, Principal principal) throws IOException {

		String photoUrl = farmPhotoService.uploadFarmPhoto(principal.getName(), farmId, file);

		Map<String, String> response = new HashMap<>();
		response.put("message", "Foto da fazenda atualizada com sucesso");
		response.put("photoUrl", photoUrl);
		return ResponseEntity.ok(response);
	}

}
