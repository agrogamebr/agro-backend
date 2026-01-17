package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.dto.ProductionUnitCreateUpdateDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitTypeDTO;
import br.com.agrogame.agrogame.service.ProductionUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/production-units")
@Tag(name = "Production Units", description = "API de unidades produtivas")
public class ProductionUnitController {

    @Autowired
    private ProductionUnitService productionUnitService;

    // -----------------------------------------------------------
    // LISTAR TIPOS (Com padrão items + count)
    // -----------------------------------------------------------
    @Operation(summary = "Listar tipos de unidades produtivas", 
               description = "Retorna todos os tipos de unidades produtivas ativos.")
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> listProductionUnitTypes() {
        
        List<ProductionUnitTypeDTO> list = productionUnitService.listProductionUnitTypes();
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", list);
        response.put("count", list.size());
        
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------
    // LISTAR UNIDADES DO USUÁRIO (Com padrão items + count)
    // -----------------------------------------------------------
    @Operation(summary = "Listar unidades produtivas", 
            description = "Lista unidades com filtros opcionais: fazenda, nome (contém) e status.")
 @GetMapping
 public ResponseEntity<Map<String, Object>> listProductionUnits(
         @Parameter(description = "ID da fazenda para filtrar") 
         @RequestParam(required = false) Integer farmId,
         
         @Parameter(description = "Nome da unidade (filtro parcial/case insensitive)") 
         @RequestParam(required = false) String name,
         
         @Parameter(description = "Status: true para ativos, false para inativos") 
         @RequestParam(required = false) Boolean isActive,
         
         Principal principal) {
     
     // Passando todos os filtros para a Service
     List<ProductionUnitDetailDTO> list = productionUnitService.listProductionUnits(
             principal.getName(), 
             farmId, 
             name, 
             isActive
     );
     
     Map<String, Object> response = new HashMap<>();
     response.put("items", list);
     response.put("count", list.size());
     
     return ResponseEntity.ok(response);
 }


    // -----------------------------------------------------------
    // MÉTODOS CRUD (Mantidos igual, retornam objeto direto)
    // -----------------------------------------------------------
    
    @Operation(summary = "Cadastrar unidade produtiva", description = "Cria uma unidade produtiva vinculada a uma fazenda.")
    @PostMapping
    public ResponseEntity<ProductionUnitDetailDTO> createProductionUnit(
            @Valid @RequestBody ProductionUnitCreateUpdateDTO body, 
            Principal principal) {
        
        ProductionUnitDetailDTO dto = productionUnitService.createProductionUnit(principal.getName(), body);
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(summary = "Detalhar unidade produtiva")
    @GetMapping("/{unitId}")
    public ResponseEntity<ProductionUnitDetailDTO> getProductionUnit(
            @PathVariable Integer unitId, 
            Principal principal) {
        
        ProductionUnitDetailDTO dto = productionUnitService.getProductionUnit(principal.getName(), unitId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Atualizar unidade produtiva")
    @PutMapping("/{unitId}")
    public ResponseEntity<ProductionUnitDetailDTO> updateProductionUnit(
            @PathVariable Integer unitId,
            @Valid @RequestBody ProductionUnitCreateUpdateDTO body, 
            Principal principal) {
        
        ProductionUnitDetailDTO dto = productionUnitService.updateProductionUnit(principal.getName(), unitId, body);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Excluir (desativar) unidade produtiva")
    @DeleteMapping("/{unitId}")
    public ResponseEntity<ProductionUnitDetailDTO> deleteProductionUnit(
            @PathVariable Integer unitId, 
            Principal principal) {
        
        ProductionUnitDetailDTO dto = productionUnitService.deleteProductionUnit(principal.getName(), unitId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Reativar unidade produtiva")
    @PatchMapping("/{unitId}/reactivate")
    public ResponseEntity<ProductionUnitDetailDTO> reactivateProductionUnit(
            @PathVariable Integer unitId, 
            Principal principal) {
        
        ProductionUnitDetailDTO dto = productionUnitService.reactivateProductionUnit(principal.getName(), unitId);
        return ResponseEntity.ok(dto);
    }
}
