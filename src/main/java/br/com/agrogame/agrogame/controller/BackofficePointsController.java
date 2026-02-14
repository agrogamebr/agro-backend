//package br.com.agrogame.agrogame.controller;
//
//import java.security.Principal;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.agrogame.agrogame.dto.BackofficePointsStatementDTO;
//import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
//import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
//import br.com.agrogame.agrogame.model.User;
//import br.com.agrogame.agrogame.repository.UserRepository;
//import br.com.agrogame.agrogame.service.BackofficePointsService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//@RestController
//@RequestMapping("/api/backoffice/points")
//@Tag(name = "Backoffice", description = "Gestão administrativa (Atividades, Pontos, Extratos)") 
//public class BackofficePointsController {
//
//	@Autowired
//	private BackofficePointsService service;
//
//	@Autowired
//	private UserRepository userRepository;
//
//	@Operation(summary = "Extrato detalhado de pontos", description = "Retorna lista de transações e o saldo atual do produtor selecionado.")
//	@GetMapping("/statement")
//	public ResponseEntity<Map<String, Object>> getStatement(@RequestParam(required = false) Integer producerId,
//			@RequestParam(required = false) Integer farmId, @RequestParam(required = false) Integer productionUnitId,
//			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//			@RequestParam(required = false) String operationType, @RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "20") int size, Principal principal) {
//
//		User operator = userRepository.findByEmail1(principal.getName())
//				.orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado"));
//
//		// 1. Busca o Extrato Paginado
//		Page<BackofficePointsStatementDTO> pageResult = service.getStatement(operator, producerId, farmId,
//				productionUnitId, startDate, endDate, operationType, page, size);
//
//		// 2. Busca o Saldo Atual (para o rodapé)
//		ProducerPointsBalanceDTO balanceDTO = service.getProducerBalance(operator, producerId, farmId);
//
//		Map<String, Object> response = new HashMap<>();
//		response.put("items", pageResult.getContent());
//		response.put("page", pageResult.getNumber());
//		response.put("size", pageResult.getSize());
//		response.put("totalElements", pageResult.getTotalElements());
//		response.put("totalPages", pageResult.getTotalPages());
//		response.put("currentBalance", balanceDTO.getCurrentBalance()); // Saldo no rodapé
//
//		return ResponseEntity.ok(response);
//	}
//}
