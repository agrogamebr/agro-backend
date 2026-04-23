package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.WorkerCreateDTO;
import br.com.agrogame.agrogame.dto.WorkerDetailDTO;
import br.com.agrogame.agrogame.dto.WorkerUpdateDTO;
import br.com.agrogame.agrogame.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/workers")
@Tag(name = "Workers", description = "Cadastro e gestão de workers (auxiliares) pelos produtores rurais.")
public class WorkerController {

	@Autowired
	private WorkerService workerService;

	@Operation(summary = "Listar workers do produtor", description = """
			Lista todos os workers vinculados às fazendas/unidades do produtor logado.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "403", description = "Usuário logado não é produtor rural") })
	@GetMapping
	public ResponseEntity<Map<String, Object>> listWorkers(@RequestParam(required = false) Integer farmId,
			@RequestParam(required = false) Integer workerId, Principal principal) {

		List<WorkerDetailDTO> items = workerService.listWorkers(principal.getName(), farmId, workerId);

		Map<String, Object> response = new HashMap<>();
		response.put("items", items);
		response.put("total", items.size());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Cadastrar worker", description = """
			Cria um novo worker vinculado ao produtor logado e, opcionalmente, às unidades produtivas informadas.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Worker cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos (email/senha)"),
			@ApiResponse(responseCode = "403", description = "Usuário logado não é produtor rural"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado") })
	@PostMapping
	public ResponseEntity<Map<String, Object>> registerWorker(@Valid @RequestBody WorkerCreateDTO dto,
			Principal principal) {

		WorkerDetailDTO worker = workerService.registerWorker(principal.getName(), dto);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Worker cadastrado com sucesso");
		response.put("worker", worker);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Editar worker", description = """
			Atualiza apenas os campos enviados no corpo. Campos omitidos permanecem com o valor atual. E‑mail não é alterável por este endpoint.
			O próprio worker pode editar apenas seus próprios dados.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Worker atualizado com sucesso"),
			@ApiResponse(responseCode = "403", description = "Sem permissão para editar este worker"),
			@ApiResponse(responseCode = "404", description = "Worker não encontrado") })
	@PutMapping("/{workerId}")
	public ResponseEntity<WorkerDetailDTO> updateWorker(@PathVariable Integer workerId,
			@Valid @RequestBody WorkerUpdateDTO dto, Principal principal) {

		WorkerDetailDTO updated = workerService.updateWorker(principal.getName(), workerId, dto);
		return ResponseEntity.ok(updated);
	}

//	@Operation(summary = "Associar worker a unidades produtivas", description = """
//			Define as unidades produtivas às quais o worker estará vinculado.
//			Somente o produtor dono do worker pode alterar esses vínculos.
//			""")
//	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Vínculos atualizados com sucesso"),
//			@ApiResponse(responseCode = "403", description = "Sem permissão"),
//			@ApiResponse(responseCode = "404", description = "Worker não encontrado") })
//	@PostMapping("/{workerId}/production-units")
//	public ResponseEntity<Map<String, Object>> assignUnits(@PathVariable Integer workerId,
//			@Valid @RequestBody WorkerUnitsAssignDTO dto, Principal principal) {
//
//		workerService.assignUnit(principal.getName(), workerId, dto.getProductionUnitId());
//
//		Map<String, Object> response = new HashMap<>();
//		response.put("success", true);
//		response.put("message", "Unidades produtivas associadas com sucesso");
//
//		return ResponseEntity.ok(response);
//	}

	@Operation(summary = "Inativar worker", description = """
			Realiza delete lógico do worker (user_status_id = INACTIVE),
			impedindo novo login.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Worker inativado com sucesso"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Worker não encontrado") })
	@DeleteMapping("/{workerId}")
	public ResponseEntity<Map<String, Object>> deactivateWorker(@PathVariable Integer workerId, Principal principal) {

		workerService.deactivateWorker(principal.getName(), workerId);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Worker inativado com sucesso");

		return ResponseEntity.ok(response);
	}
}
