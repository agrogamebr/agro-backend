package br.com.agrogame.agrogame.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.FarmCreateUpdateDTO;
import br.com.agrogame.agrogame.dto.FarmDetailDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.CropType;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.FarmCrop;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.CropTypeRepository;
import br.com.agrogame.agrogame.repository.FarmCropRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.auth.util.IdentifierValidator;

@Service
public class FarmService {

	private final FarmRepository farmRepository;
	private final FarmCropRepository farmCropRepository;
	private final UserRepository userRepository;
	private final CropTypeRepository cropTypeRepository;
	private final UserActivityRepository userActivityRepository;
	private final ProductionUnitRepository productionUnitRepository;

	public FarmService(FarmRepository farmRepository, FarmCropRepository farmCropRepository,
			UserRepository userRepository, CropTypeRepository cropTypeRepository,
			UserActivityRepository userActivityRepository, ProductionUnitRepository productionUnitRepository) {
		this.farmRepository = farmRepository;
		this.farmCropRepository = farmCropRepository;
		this.userRepository = userRepository;
		this.cropTypeRepository = cropTypeRepository;
		this.userActivityRepository = userActivityRepository;
		this.productionUnitRepository = productionUnitRepository;
	}

	/**
	 * Cria uma fazenda vinculada ao usuário autenticado (producer).
	 */
	@Transactional
	public FarmDetailDTO createFarm(String username, FarmCreateUpdateDTO dto) {
		User user = loadProducerUser(username);

		Farm farm = new Farm();
		farm.setOwner(user);
		farm.setCompany(user.getCompany()); // supondo getCompany()
		farm.setName(dto.getName());
		farm.setArea(dto.getArea());
		farm.setDescription(dto.getDescription());
		farm.setCity(dto.getCity());
		farm.setState(dto.getState());
		farm.setZipcode(IdentifierValidator.normalizeZipcode(dto.getZipcode()));
		farm.setLatitude(dto.getLatitude());
		farm.setLongitude(dto.getLongitude());
		farm.setIsActive(true);
		farm.setCreatedAt(LocalDateTime.now());
		farm.setCreatedBy(user.getId());

		Farm saved = farmRepository.save(farm);

		// vincular culturas (farm_crops)
		saveFarmCrops(saved, dto.getCropTypeIds(), user, dto.getArea());

		return toDetailDTO(saved, dto.getCropTypeIds());
	}

	/**
	 * Lista as fazendas do usuário autenticado.
	 */
	@Transactional(readOnly = true)
	public List<FarmDetailDTO> listFarms(String username) {
		User user = loadCurrentUser(username);

		if (EnumUserType.PRODUCER.getCode().equals(user.getUserType().getCode())) {
			List<Farm> farms = farmRepository.findByOwnerIdAndIsActiveTrue(user.getId());
			return mapFarmsToDTO(farms);
		}

		if (EnumUserType.WORKER.getCode().equals(user.getUserType().getCode())) {
			List<Farm> farms = farmRepository.findByWorkerAssignments(user.getId());
			return mapFarmsToDTO(farms);
		}

		throw new BusinessException("Tipo de usuário não permitido para listar fazendas");
	}

	@Transactional(readOnly = true)
	public FarmDetailDTO getFarm(String username, Integer farmId) {
		User user = loadCurrentUser(username);
		Farm farm;

		if (EnumUserType.PRODUCER.getCode().equals(user.getUserType().getCode())) {
			farm = farmRepository.findByIdAndOwnerIdAndIsActiveTrue(farmId, user.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
		} else if (EnumUserType.WORKER.getCode().equals(user.getUserType().getCode())) {
			farm = farmRepository.findByIdAndWorkerAssignments(farmId, user.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada para esse usuário"));
		} else {
			throw new BusinessException("Tipo de usuário não permitido para visualizar fazendas");
		}

		List<Integer> cropTypeIds = farmCropRepository.findByFarmId(farm.getId()).stream()
				.map(fc -> fc.getCropType().getId()).toList();

		return toDetailDTO(farm, cropTypeIds);
	}

	@Transactional
	public FarmDetailDTO updateFarm(String username, Integer farmId, FarmCreateUpdateDTO dto) {
		User user = loadProducerUser(username);
		Farm farm = farmRepository.findByIdAndOwnerIdAndIsActiveTrue(farmId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		// atualizar campos da farm
		farm.setName(dto.getName());
		farm.setArea(dto.getArea());
		farm.setDescription(dto.getDescription());
		farm.setCity(dto.getCity());
		farm.setState(dto.getState());
		farm.setZipcode(IdentifierValidator.normalizeZipcode(dto.getZipcode()));
		farm.setLatitude(dto.getLatitude());
		farm.setLongitude(dto.getLongitude());
		farm.setUpdatedAt(LocalDateTime.now());
		farm.setUpdatedBy(user.getId());

		// JPA faz UPDATE automático
		Farm saved = farmRepository.save(farm);

		// deletar farm_crops antigas
		farmCropRepository.deleteByFarmId(saved.getId());

		// criar farm_crops novas
		saveFarmCrops(saved, dto.getCropTypeIds(), user, dto.getArea());

		return toDetailDTO(saved, dto.getCropTypeIds());
	}

	/**
	 * Remove (soft delete) uma fazenda e CANCELA atividades pendentes em cascata.
	 */
	@Transactional
	public FarmDetailDTO deleteFarm(String username, Integer farmId) {
		User user = loadProducerUser(username);
		Farm farm = farmRepository.findByIdAndOwnerIdAndIsActiveTrue(farmId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		// 1. Soft Delete da Fazenda
		farm.setIsActive(false);
		farm.setDeactivatedAt(LocalDateTime.now());
		farm.setDeactivatedBy(user.getId());
		Farm saved = farmRepository.save(farm);

		// 2. Cascata: Desativar Unidades Produtivas
		List<ProductionUnit> units = productionUnitRepository.findByFarmIdAndIsActiveTrue(farmId);

		for (ProductionUnit unit : units) {
			// Soft delete da unidade
			unit.setIsActive(false);

			// AGORA PODEMOS DESCOMENTAR (As colunas existem no banco)
			unit.setDeactivatedAt(LocalDateTime.now());
			unit.setDeactivatedBy(user.getId());

			productionUnitRepository.save(unit);

			// 3. Cascata: Cancelar atividades específicas desta Unidade
			// (Agora funciona pois temos production_unit_id em user_activities)
			userActivityRepository.cancelActivitiesByProductionUnit(unit.getId());
		}

		// 4. Cascata: Cancelar quaisquer outras atividades da Fazenda que não tenham
		// unidade específica
		// (Ou garantir que tudo foi limpo)
		userActivityRepository.cancelActivitiesByFarm(farmId);

		// Retorno
		List<Integer> cropTypeIds = farmCropRepository.findByFarmId(saved.getId()).stream()
				.map(fc -> fc.getCropType().getId()).toList();

		return toDetailDTO(saved, cropTypeIds);
	}

	/**
	 * Carrega o usuário e garante que é PRODUCER (user_type_id = 8, por exemplo).
	 */
	private User loadProducerUser(String username) {
		User user = userRepository.findByEmail1(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		if (user.getUserType() == null || !EnumUserType.PRODUCER.getCode().equals(user.getUserType().getCode())) {
			throw new BusinessException("Apenas produtores podem gerenciar fazendas");
		}

		if (user.getCompany() == null) {
			throw new BusinessException("Usuário não está vinculado a uma empresa");
		}

		return user;
	}

	private User loadCurrentUser(String username) {
		return userRepository.findByEmail1(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
	}

	private List<FarmDetailDTO> mapFarmsToDTO(List<Farm> farms) {
		return farms.stream().map(farm -> {
			List<Integer> cropTypeIds = farmCropRepository.findByFarmId(farm.getId()).stream()
					.map(fc -> fc.getCropType().getId()).toList();
			return toDetailDTO(farm, cropTypeIds);
		}).toList();
	}

	/**
	 * Cria os registros de farm_crops para a fazenda.
	 */
	private void saveFarmCrops(Farm farm, List<Integer> cropTypeIds, User user, BigDecimal plantedArea) {

		if (cropTypeIds == null || cropTypeIds.isEmpty()) {
			return;
		}

		List<CropType> cropTypes = cropTypeRepository.findAllById(cropTypeIds);
		if (cropTypes.size() != cropTypeIds.size()) {
			throw new BusinessException("Uma ou mais culturas são inválidas");
		}

		for (CropType cropType : cropTypes) {
			FarmCrop fc = new FarmCrop();
			fc.setFarm(farm);
			fc.setCropType(cropType);
			fc.setPlantedArea(plantedArea);
			fc.setYear(LocalDateTime.now().getYear());
			fc.setCreatedAt(LocalDateTime.now());
			fc.setCreatedBy(user.getId());
			farmCropRepository.save(fc);
		}
	}

	/**
	 * Converte entidade para DTO de resposta.
	 */
	private FarmDetailDTO toDetailDTO(Farm farm, List<Integer> cropTypeIds) {
		FarmDetailDTO dto = new FarmDetailDTO();
		dto.setId(farm.getId());
		dto.setName(farm.getName());
		dto.setArea(farm.getArea());
		dto.setDescription(farm.getDescription());
		dto.setState(farm.getState());
		dto.setCity(farm.getCity());
		dto.setZipcode(farm.getZipcode());
		dto.setLatitude(farm.getLatitude());
		dto.setLongitude(farm.getLongitude());
		dto.setActive(Boolean.TRUE.equals(farm.getIsActive()));
		dto.setCropTypeIds(cropTypeIds);
		dto.setThumbnailGsUrl(farm.getThumbnailGsUrl());

		List<ProductionUnit> units = productionUnitRepository.findByFarmIdAndIsActiveTrue(farm.getId());

		List<ProductionUnitDetailDTO> unitsDto = units.stream().map(this::toUnitDetailDTO).toList();

		dto.setProductionUnits(unitsDto);
		return dto;
	}

	@Transactional
	public FarmDetailDTO reactivateFarm(String username, Integer farmId) {
		User user = loadProducerUser(username);

		Farm farm = farmRepository.findByIdAndOwnerIdAndIsActiveFalse(farmId, user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada ou já está ativa"));

		farm.setIsActive(true);
		farm.setDeactivatedAt(null);
		farm.setDeactivatedBy(null);
		farm.setUpdatedAt(LocalDateTime.now());
		farm.setUpdatedBy(user.getId());

		Farm saved = farmRepository.save(farm);

		List<Integer> cropTypeIds = farmCropRepository.findByFarmId(saved.getId()).stream()
				.map(fc -> fc.getCropType().getId()).toList();

		return toDetailDTO(saved, cropTypeIds);
	}

	private ProductionUnitDetailDTO toUnitDetailDTO(ProductionUnit unit) {
		ProductionUnitDetailDTO dto = new ProductionUnitDetailDTO();
		dto.setId(unit.getId());
		dto.setName(unit.getName());
		dto.setDescription(unit.getDescription());
		dto.setArea(unit.getArea());
		dto.setIsActive(unit.getIsActive());
		dto.setThumbnailGsUrl(unit.getThumbnailGsUrl());
		dto.setCreatedAt(unit.getCreatedAt());

		dto.setFarmId(unit.getFarm().getId());
		dto.setFarmName(unit.getFarm().getName());

		if (unit.getProductionUnitType() != null) {
			dto.setProductionUnitTypeId(unit.getProductionUnitType().getId());
			dto.setProductionUnitTypeName(unit.getProductionUnitType().getName());
			dto.setProductionUnitTypeCode(unit.getProductionUnitType().getCode());
		}

		if (unit.getCropType() != null) {
			dto.setCropTypeId(unit.getCropType().getId());
			dto.setCropTypeName(unit.getCropType().getName());
		}

		return dto;
	}

}
