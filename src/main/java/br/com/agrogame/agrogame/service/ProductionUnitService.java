package br.com.agrogame.agrogame.service;

import br.com.agrogame.agrogame.dto.ProductionUnitCreateUpdateDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitTypeDTO;
import br.com.agrogame.agrogame.exceptions.AuthenticationException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.ProductionUnitType;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitTypeRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

		// 3. Criar e Salvar
		ProductionUnit unit = new ProductionUnit();
		unit.setFarm(farm);
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
	public List<ProductionUnitDetailDTO> listProductionUnits(String email, Integer farmIdFilter) {
		User user = getProducerOrThrow(email);
		List<ProductionUnit> units;

		if (farmIdFilter != null) {
			// Se informou filtro de fazenda, valida acesso
			Farm farm = farmRepository.findById(farmIdFilter)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

			if (!farm.getOwner().getId().equals(user.getId())) {
				throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para acessar esta fazenda.");
			}
			// Busca específica
			units = productionUnitRepository.findByFarmIdAndOwnerId(farmIdFilter, user.getId());
		} else {
			// Busca todas do produtor
			units = productionUnitRepository.findByOwnerId(user.getId());
		}

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

		// 3. Atualizar dados básicos
		unit.setName(body.getName());
		unit.setDescription(body.getDescription());
		unit.setArea(body.getArea());
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
		unit = productionUnitRepository.save(unit);
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

		unit = productionUnitRepository.save(unit);
		return toDetailDTO(unit);
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
