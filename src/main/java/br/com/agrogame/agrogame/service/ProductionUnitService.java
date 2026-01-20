package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.ProductionUnitCreateUpdateDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitTypeDTO;
import br.com.agrogame.agrogame.exceptions.AuthenticationException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.ProductionUnitType;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitTypeRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class ProductionUnitService {

	@Autowired
	private ProductionUnitRepository productionUnitRepository;

	@Autowired
	private ProductionUnitTypeRepository productionUnitTypeRepository;

	@Autowired
	private FarmRepository farmRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CropTypeRepository cropTypeRepository;

	@Autowired
	private UserActivityRepository userActivityRepository;

	/**
	 * Método auxiliar para buscar e validar o produtor logado.
	 */
	private User getProducerOrThrow(String email) {
		// Busca pelo email1 carregando o userType via FETCH (Query do seu repository)
		User user = userRepository.findByEmail1WithUserType(email).orElseThrow(
				() -> new AuthenticationException("USER_NOT_FOUND", "Usuário não encontrado com o email: " + email));

		// Valida se é Produtor Rural (ID 8)
		if (user.getUserType() == null || !user.getUserType().getId().equals(8)) {
			throw new AuthenticationException("FORBIDDEN", "Apenas produtores rurais podem acessar este recurso.");
		}
		return user;
	}

	// ----------------------------------------------------------------------------------
	// CRUD DE UNIDADES PRODUTIVAS
	// ----------------------------------------------------------------------------------

	@Transactional
	public ProductionUnitDetailDTO createProductionUnit(String email, ProductionUnitCreateUpdateDTO body) {
		User user = getProducerOrThrow(email);

		// 1. Validar Fazenda
		Farm farm = farmRepository.findById(body.getFarmId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		// Validar Dono da Fazenda
		if (!farm.getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para criar unidades nesta fazenda.");
		}

		// Validar se Fazenda está ativa
		if (!Boolean.TRUE.equals(farm.getIsActive())) {
			throw new BusinessException("Não é possível criar unidades em uma fazenda desativada.");
		}

		// 2. Validar Tipo de Unidade
		ProductionUnitType type = productionUnitTypeRepository.findById(body.getProductionUnitTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de unidade produtiva não encontrado"));

		if (!Boolean.TRUE.equals(type.getIsActive())) {
			throw new BusinessException("Este tipo de unidade produtiva está inativo.");
		}

		if (body.getCropTypeId() == null) {
			throw new BusinessException("O tipo de cultura (cropType) é obrigatório.");
		}

		// 1. BUSCAR O CROP TYPE (Faltava isso)
		CropType cropType = cropTypeRepository.findById(body.getCropTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado"));

		// 3. Criar e Salvar
		ProductionUnit unit = new ProductionUnit();
		unit.setFarm(farm);
		unit.setCropType(cropType);
		unit.setName(body.getName());
		unit.setProductionUnitType(type);
		unit.setName(body.getName());
		unit.setDescription(body.getDescription());
		unit.setArea(body.getArea());
		unit.setIsActive(true);
		unit.setCreatedAt(LocalDateTime.now());
		unit.setCreatedBy(user.getId()); // ID Integer do user

		unit = productionUnitRepository.save(unit);

		return toDetailDTO(unit);
	}

	@Transactional(readOnly = true)
	public List<ProductionUnitDetailDTO> listProductionUnits(String email, Integer farmIdFilter, String nameFilter,
			Boolean isActiveFilter) {
		// 1. Identificar o usuário
		User user = getProducerOrThrow(email);

		// 2. Se informou filtro de fazenda, mantemos a validação de segurança (boa
		// prática)
		if (farmIdFilter != null) {
			Farm farm = farmRepository.findById(farmIdFilter)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

			if (!farm.getOwner().getId().equals(user.getId())) {
				throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para acessar esta fazenda.");
			}
		}

		String nameLike = null;
		if (nameFilter != null && !nameFilter.trim().isEmpty()) {
			nameLike = "%" + nameFilter.toLowerCase() + "%";
		}
		// 3. Busca unificada usando a Query do Repositório (que trata os nulos)
		List<ProductionUnit> units = productionUnitRepository.findByFilters(user.getId(), farmIdFilter, nameLike,
				isActiveFilter);

		// 4. Converte para DTO
		return units.stream().map(this::toDetailDTO).toList();
	}

	@Transactional(readOnly = true)
	public ProductionUnitDetailDTO getProductionUnit(String email, Integer unitId) {
		User user = getProducerOrThrow(email);

		ProductionUnit unit = productionUnitRepository.findById(unitId)
				.orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

		// Validar ownership através da fazenda
		if (!unit.getFarm().getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN",
					"Você não tem permissão para acessar esta unidade produtiva.");
		}

		return toDetailDTO(unit);
	}

	@Transactional
	public ProductionUnitDetailDTO updateProductionUnit(String email, Integer unitId,
			ProductionUnitCreateUpdateDTO body) {
		User user = getProducerOrThrow(email);

		ProductionUnit unit = productionUnitRepository.findById(unitId)
				.orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

		// Validar ownership
		if (!unit.getFarm().getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN",
					"Você não tem permissão para editar esta unidade produtiva.");
		}

		if (!Boolean.TRUE.equals(unit.getIsActive())) {
			throw new BusinessException("Não é possível editar uma unidade produtiva desativada.");
		}

		// 1. Validar mudança de fazenda (se houver)
		if (!body.getFarmId().equals(unit.getFarm().getId())) {
			Farm newFarm = farmRepository.findById(body.getFarmId())
					.orElseThrow(() -> new ResourceNotFoundException("Nova fazenda não encontrada"));

			if (!newFarm.getOwner().getId().equals(user.getId())) {
				throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para mover para esta fazenda.");
			}
			if (!Boolean.TRUE.equals(newFarm.getIsActive())) {
				throw new BusinessException("Não é possível mover para uma fazenda desativada.");
			}
			unit.setFarm(newFarm);
		}

		// 2. Validar mudança de tipo (se houver)
		if (!body.getProductionUnitTypeId().equals(unit.getProductionUnitType().getId())) {
			ProductionUnitType newType = productionUnitTypeRepository.findById(body.getProductionUnitTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("Novo tipo de unidade não encontrado"));

			if (!Boolean.TRUE.equals(newType.getIsActive())) {
				throw new BusinessException("O novo tipo de unidade produtiva está inativo.");
			}
			unit.setProductionUnitType(newType);
		}

		// 3. Mudança de CropType (NOVA LÓGICA)
		if (body.getCropTypeId() != null) {
			// Se o ID for diferente do atual ou o atual for null
			if (unit.getCropType() == null || !body.getCropTypeId().equals(unit.getCropType().getId())) {
				CropType newCrop = cropTypeRepository.findById(body.getCropTypeId())
						.orElseThrow(() -> new ResourceNotFoundException("Tipo de cultura não encontrado"));

				// Opcional: Validar se a nova fazenda realmente produz essa cultura (verificar
				// na tabela farm_crops)
				// Se não tiver essa validação rígida agora, apenas setamos:
				unit.setCropType(newCrop);
			}
		}

		// 3. Atualizar dados básicos
		unit.setName(body.getName());
		unit.setDescription(body.getDescription());
		unit.setArea(body.getArea());
		unit.setUpdatedAt(LocalDateTime.now());
		unit.setUpdatedBy(user.getId());
		unit = productionUnitRepository.save(unit);

		return toDetailDTO(unit);
	}

	@Transactional
	public ProductionUnitDetailDTO deleteProductionUnit(String email, Integer unitId) {
		User user = getProducerOrThrow(email);

		ProductionUnit unit = productionUnitRepository.findById(unitId)
				.orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

		if (!unit.getFarm().getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN",
					"Você não tem permissão para deletar esta unidade produtiva.");
		}

		if (!Boolean.TRUE.equals(unit.getIsActive())) {
			throw new BusinessException("A unidade produtiva já está desativada.");
		}

		// Soft Delete
		unit.setIsActive(false);
		unit.setUpdatedAt(LocalDateTime.now());
		unit.setUpdatedBy(user.getId());
		unit = productionUnitRepository.save(unit);

		// Cascata: Cancelar Activities da Unit
		userActivityRepository.cancelActivitiesByProductionUnit(unitId);

		return toDetailDTO(unit);
	}

	@Transactional
	public ProductionUnitDetailDTO reactivateProductionUnit(String email, Integer unitId) {
		User user = getProducerOrThrow(email);

		ProductionUnit unit = productionUnitRepository.findById(unitId)
				.orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

		if (!unit.getFarm().getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN",
					"Você não tem permissão para reativar esta unidade produtiva.");
		}

		if (Boolean.TRUE.equals(unit.getIsActive())) {
			throw new BusinessException("A unidade produtiva já está ativa.");
		}

		// Não pode reativar unit se a fazenda estiver inativa
		if (!Boolean.TRUE.equals(unit.getFarm().getIsActive())) {
			throw new BusinessException("Não é possível reativar uma unidade de uma fazenda desativada.");
		}

		unit.setIsActive(true);
		unit.setDeactivatedAt(null);
		unit.setDeactivatedBy(null);
		unit.setUpdatedAt(LocalDateTime.now());
		unit.setUpdatedBy(user.getId());
		unit = productionUnitRepository.save(unit);

		return toDetailDTO(unit);
	}

	@Transactional(readOnly = true)
	public List<ProductionUnitTypeDTO> listCompatibleUnitTypes(String email, Integer farmId) {
		User user = getProducerOrThrow(email);

		// 1. Validar se a fazenda existe e pertence ao usuário
		Farm farm = farmRepository.findById(farmId)
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		if (!farm.getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para acessar dados desta fazenda.");
		}

		// 2. Buscar tipos compatíveis direto do banco
		List<ProductionUnitType> compatibleTypes = productionUnitTypeRepository.findCompatibleTypesByFarmId(farmId);

		// 3. Converter para DTO
		return compatibleTypes.stream().map(this::toTypeDTO).toList();
	}

	// ----------------------------------------------------------------------------------
	// LISTAGEM DE TIPOS (Auxiliar para combo box)
	// ----------------------------------------------------------------------------------

	@Transactional(readOnly = true)
	public List<ProductionUnitTypeDTO> listProductionUnitTypes() {
		return productionUnitTypeRepository.findByIsActiveTrueOrderByName().stream().map(this::toTypeDTO).toList();
	}

	private ProductionUnitDetailDTO toDetailDTO(ProductionUnit unit) {
		ProductionUnitDetailDTO dto = new ProductionUnitDetailDTO();
		dto.setId(unit.getId());
		dto.setFarmId(unit.getFarm().getId());
		dto.setFarmName(unit.getFarm().getName());
		dto.setProductionUnitTypeId(unit.getProductionUnitType().getId());
		dto.setProductionUnitTypeName(unit.getProductionUnitType().getName());
		dto.setProductionUnitTypeCode(unit.getProductionUnitType().getCode());
		dto.setName(unit.getName());
		dto.setDescription(unit.getDescription());
		dto.setArea(unit.getArea());
		dto.setIsActive(unit.getIsActive());
		dto.setCreatedAt(unit.getCreatedAt());
		if (unit.getCropType() != null) {
			dto.setCropTypeId(unit.getCropType().getId());
			dto.setCropTypeName(unit.getCropType().getName());
		}
		return dto;
	}

	private ProductionUnitTypeDTO toTypeDTO(ProductionUnitType type) {
		ProductionUnitTypeDTO dto = new ProductionUnitTypeDTO();
		dto.setId(type.getId());
		dto.setCode(type.getCode());
		dto.setName(type.getName());
		dto.setDescription(type.getDescription());
		dto.setIsActive(type.getIsActive());
		return dto;
	}
}
