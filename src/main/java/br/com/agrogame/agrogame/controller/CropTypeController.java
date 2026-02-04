package br.com.agrogame.agrogame.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.service.AccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/crop-types")
@Tag(name = "Crop Types", description = "Endpoints para gestão de tipos de cultura")
public class CropTypeController {

    private final CropTypeRepository cropTypeRepository;
    
    private final UserRepository userRepository;
    
    private final AccessControlService accessControlService;

    public CropTypeController(CropTypeRepository cropTypeRepository, UserRepository userRepository, AccessControlService accessControlService) {
        this.cropTypeRepository = cropTypeRepository;
		this.userRepository = userRepository;
		this.accessControlService = accessControlService;
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
    
    @GetMapping("/list-by-company")
    public ResponseEntity<Map<String, Object>> listCropTypesByCompany(Principal principal) {

        User currentUser = userRepository.findByEmail1(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (currentUser.getCompany() == null) {
            throw new BusinessException("Usuário não vinculado a nenhuma empresa.");
        }
        
        this.accessControlService.validateBackofficeUser(currentUser);

        Integer companyId = currentUser.getCompany().getId();

        List<CropType> cropTypes = cropTypeRepository.findDistinctByCompanyId(companyId);

        Map<String, Object> response = Map.of(
            "crop_types", cropTypes,
            "total", cropTypes.size()
        );

        return ResponseEntity.ok(response);
    }

}
