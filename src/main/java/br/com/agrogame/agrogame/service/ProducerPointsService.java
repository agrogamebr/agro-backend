package br.com.agrogame.agrogame.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
import br.com.agrogame.agrogame.dto.ProducerPointsTransactionDTO;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserPointsTransactionRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ProducerPointsService {

	private final UserRepository userRepository;
	private final UserPointsTransactionRepository userPointsTransactionRepository;

	public ProducerPointsService(UserRepository userRepository,
			UserPointsTransactionRepository userPointsTransactionRepository) {
		this.userRepository = userRepository;
		this.userPointsTransactionRepository = userPointsTransactionRepository;
	}

	/**
	 * Retorna o saldo atual de pontos do produtor (somente do usuário autenticado).
	 * Se farmId for informado, retorna saldo filtrado apenas daquela fazenda.
	 */
	public ProducerPointsBalanceDTO getCurrentBalance(Integer userId, Integer farmId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Integer balance;

		if (farmId != null) {
			// Buscar saldo apenas da farm específica
			balance = userPointsTransactionRepository.findLatestBalanceByUserAndFarm(userId, farmId);
		} else {
			// Buscar saldo total do produtor
			balance = userPointsTransactionRepository.findLatestBalance(userId);
		}

		if (balance == null) {
			balance = 0;
		}

		return new ProducerPointsBalanceDTO(user.getId(), user.getFullName(), balance);
	}

	/**
	 * Lista o histórico de transações de pontos do produtor (usuário autenticado).
	 * Se farmId for informado, filtra apenas transações daquela fazenda.
	 */
	public Page<ProducerPointsTransactionDTO> getTransactions(Integer userId, Integer farmId, Pageable pageable) {
		return userPointsTransactionRepository.findByUserAndFarmFilters(userId, farmId, pageable);
	}

}
