package br.com.agrogame.agrogame.controller;

import br.com.agrogame.agrogame.dto.CreateFarmActivityDTO;
import br.com.agrogame.agrogame.dto.FarmActivityResponseDTO;
import br.com.agrogame.agrogame.service.FarmActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farm-activity")
@Tag(name = "Farm Activity", description = "Endpoints para cadastro de atividades")
public class FarmActivityController {

	private final FarmActivityService service;

	public FarmActivityController(FarmActivityService service) {
		this.service = service;
	}

	@Operation(summary = "Cadastrar atividade de fazenda", description = "Cria uma nova atividade vinculada à fazenda e tipo de cultura")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Atividade cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Erro de validação"),
			@ApiResponse(responseCode = "404", description = "Fazenda, cultura ou status não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno") })
	@PostMapping("/create-farm-activity")
	public ResponseEntity<FarmActivityResponseDTO> createFarmActivity(@Valid @RequestBody CreateFarmActivityDTO dto,
			@RequestHeader("X-User-Id") Integer createdBy) {

		FarmActivityResponseDTO response = service.registerFarmActivity(dto, createdBy);
		return ResponseEntity.status(201).body(response);
	}
}
