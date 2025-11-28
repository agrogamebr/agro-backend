package br.com.agrogame.agrogame.controller;

import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crop-types")
@Tag(name = "Crop Types", description = "Endpoints para gestão de tipos de cultura")
public class CropTypeController {

    private final CropTypeRepository cropTypeRepository;

    public CropTypeController(CropTypeRepository cropTypeRepository) {
        this.cropTypeRepository = cropTypeRepository;
    }

    @Operation(summary = "Listar tipos de cultura",
               description = "Retorna todos os tipos de cultura ativos disponíveis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tipos de cultura"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listCropTypes() {
        List<CropType> cropTypes = cropTypeRepository.findAll();
        
        Map<String, Object> response = Map.of(
            "crop_types", cropTypes,
            "total", cropTypes.size()
        );
        
        return ResponseEntity.ok(response);
    }
}
