package br.com.agrogame.agrogame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.BackofficePointsStatementDTO;
import br.com.agrogame.agrogame.dto.BackofficePointsStatementProjection;
import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserPointsTransactionRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class BackofficePointsService {

	@Autowired
	private UserPointsTransactionRepository transactionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FarmRepository farmRepository;

	public Page<BackofficePointsStatementDTO> getStatement(User operator, Integer producerId, Integer farmId,
			Integer productionUnitId, LocalDate startDate, LocalDate endDate, String operationType, int page,
			int size) {

		// 1. Regra de Negócio: Obrigatório Produtor OU Fazenda
		if (producerId == null && farmId == null) {
			throw new BusinessException("É necessário filtrar por Produtor ou por Fazenda.");
		}

		// 2. Validação de Segurança
		validateAccess(operator, producerId, farmId);

		// 3. Ajustar Datas
		LocalDateTime startDt = (startDate != null) ? startDate.atStartOfDay() : null;
		LocalDateTime endDt = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

		// 4. Buscar
		Pageable pageable = PageRequest.of(page, size);

		Page<BackofficePointsStatementProjection> results = transactionRepository.findStatementForBackoffice(producerId,
				startDt, endDt, operationType, farmId, productionUnitId, pageable);

		return results.map(this::toDTO);
	}

	public ProducerPointsBalanceDTO getProducerBalance(User operator, Integer producerId, Integer farmId) {
		if (producerId == null) {
			return new ProducerPointsBalanceDTO(null, "N/A", 0); // Sem produtor = sem saldo pessoal
		}

		// Reutiliza a validação de acesso
		validateAccess(operator, producerId, farmId);

		User producer = userRepository.findById(producerId)
				.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));

		return new ProducerPointsBalanceDTO(producer.getId(), producer.getFullName(), producer.getPointsBalance());
	}

	// Método auxiliar para centralizar validação de segurança
	private void validateAccess(User operator, Integer producerId, Integer farmId) {
		boolean isGlobalAdmin = (operator.getUserType().getId() == 1 || operator.getUserType().getId() == 2 || operator.getUserType().getId() == 3 || operator.getUserType().getId() == 10);

		if (!isGlobalAdmin) {
			if (producerId != null) {
				User producer = userRepository.findById(producerId)
						.orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado"));
				if (!operator.getCompany().getId().equals(producer.getCompany().getId())) {
					throw new AccessDeniedException("Produtor pertence a outra empresa.");
				}
			}
			if (farmId != null) {
				Farm farm = farmRepository.findById(farmId)
						.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
				if (!operator.getCompany().getId().equals(farm.getCompany().getId())) {
					throw new AccessDeniedException("Fazenda pertence a outra empresa.");
				}
			}
		}
	}

	private BackofficePointsStatementDTO toDTO(BackofficePointsStatementProjection p) {
		BackofficePointsStatementDTO dto = new BackofficePointsStatementDTO();
		dto.setId(p.getId());
		dto.setDate(p.getDate());
		dto.setDescription(p.getDescription());
		dto.setFarmName(p.getFarmName());
		dto.setProductionUnitName(p.getProductionUnitName());
		dto.setPoints(p.getPoints());
		dto.setBalanceAfter(p.getBalanceAfter());

		// Formatar visualmente
		boolean isCredit = "earn".equalsIgnoreCase(p.getOperationType())
				|| "adjust_credit".equalsIgnoreCase(p.getOperationType());
		dto.setOperationType(isCredit ? "Crédito" : "Débito");

		return dto;
	}
}