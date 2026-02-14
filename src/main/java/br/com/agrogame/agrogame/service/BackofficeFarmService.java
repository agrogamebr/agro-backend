package br.com.agrogame.agrogame.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.BackofficeFarmDTO;
import br.com.agrogame.agrogame.dto.ProductionUnitDetailDTO;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;

@Service
public class BackofficeFarmService {

	@Autowired
	private FarmRepository farmRepository;

	@Autowired
	private ProductionUnitRepository productionUnitRepository;

	@Transactional(readOnly = true)
	public Page<BackofficeFarmDTO> listFarmsByCompany(User operator, Pageable pageable) {
		Integer companyId = operator.getCompany().getId();

		// 1. Busca a PÁGINA de fazendas ativas
		Page<Farm> farmPage = farmRepository.buscarPorCompanyIdAndIsActiveTrue(companyId, pageable);

		if (farmPage.isEmpty()) {
			return Page.empty(pageable);
		}

		// 2. Coleta IDs APENAS das fazendas dessa página
		List<Integer> pageFarmIds = farmPage.getContent().stream().map(Farm::getId).toList();

		// 3. Busca UPs ativas APENAS para esses IDs (Query Otimizada)
		List<ProductionUnit> unitsInPage = productionUnitRepository.findByFarmIdInAndIsActiveTrue(pageFarmIds);

		// 4. Agrupa por FarmId
		Map<Integer, List<ProductionUnit>> unitsByFarmId = unitsInPage.stream()
				.collect(Collectors.groupingBy(pu -> pu.getFarm().getId()));

		// 5. Converte a Page<Farm> para Page<BackofficeFarmDTO>
		// O método .map() do objeto Page mantém as infos de paginação (totalElements,
		// totalPages, etc)
		return farmPage.map(farm -> {
			BackofficeFarmDTO dto = new BackofficeFarmDTO();

			dto.setId(farm.getId());
			dto.setName(farm.getName());

			if (farm.getOwner() != null) {
				dto.setOwnerId(farm.getOwner().getId());
			}

			// Pega as UPs do mapa
			List<ProductionUnit> myUnits = unitsByFarmId.getOrDefault(farm.getId(), Collections.emptyList());

			dto.setProductionUnits(myUnits.stream().map(this::toUnitDTO) // Aquele mesmo método privado de antes
					.toList());

			return dto;
		});
	}

	@Transactional(readOnly = true)
	public Page<BackofficeFarmDTO> listFarmsByCompany(User operator, Integer farmId, String name, Integer ownerId,
			Pageable pageable) {
		Integer companyId = operator.getCompany().getId();

		// TRATAMENTO DO FILTRO DE NOME (Resolve o erro do Postgres)
		String nameLike = null;
		if (name != null && !name.isBlank()) {
			nameLike = "%" + name + "%";
		}

		// Passando 'nameLike' formatado para o repositório
		Page<Farm> farmPage = farmRepository.findByFilters(companyId, farmId, nameLike, ownerId, pageable);

		if (farmPage.isEmpty()) {
			return Page.empty(pageable);
		}

		List<Integer> pageFarmIds = farmPage.getContent().stream().map(Farm::getId).toList();

		// Busca otimizada das UPs
		List<ProductionUnit> unitsInPage = productionUnitRepository.findByFarmIdInAndIsActiveTrue(pageFarmIds);

		Map<Integer, List<ProductionUnit>> unitsByFarmId = unitsInPage.stream()
				.collect(Collectors.groupingBy(pu -> pu.getFarm().getId()));

		return farmPage.map(farm -> {
			BackofficeFarmDTO dto = new BackofficeFarmDTO();
			dto.setId(farm.getId());
			dto.setName(farm.getName());

			if (farm.getOwner() != null) {
				dto.setOwnerId(farm.getOwner().getId());
			}

			List<ProductionUnit> myUnits = unitsByFarmId.getOrDefault(farm.getId(), Collections.emptyList());
			dto.setProductionUnits(myUnits.stream().map(this::toUnitDTO).toList());
			return dto;
		});
	}

	// Mantive o DTO detalhado da UP que você pediu antes
	private ProductionUnitDetailDTO toUnitDTO(ProductionUnit pu) {
		ProductionUnitDetailDTO dto = new ProductionUnitDetailDTO();
		dto.setId(pu.getId());
		dto.setFarmId(pu.getFarm().getId());
		dto.setFarmName(pu.getFarm().getName()); // Redundante aqui, mas se o DTO pede...
		dto.setName(pu.getName());
		dto.setDescription(pu.getDescription());
		dto.setArea(pu.getArea());
		dto.setIsActive(pu.getIsActive());
		dto.setCreatedAt(pu.getCreatedAt());
		dto.setDeactivatedAt(pu.getDeactivatedAt());
		dto.setThumbnailGsUrl(pu.getThumbnailGsUrl());

		// Tipos (Assumindo relacionamentos mapeados)
		if (pu.getProductionUnitType() != null) {
			dto.setProductionUnitTypeId(pu.getProductionUnitType().getId());
			dto.setProductionUnitTypeName(pu.getProductionUnitType().getName());
			dto.setProductionUnitTypeCode(pu.getProductionUnitType().getCode());
		}

		if (pu.getCropType() != null) {
			dto.setCropTypeId(pu.getCropType().getId());
			dto.setCropTypeName(pu.getCropType().getName());
		}

		return dto;
	}
}
