package br.com.agrogame.agrogame.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.ProducerPointsBalanceDTO;
import br.com.agrogame.agrogame.dto.ProducerPointsTransactionDTO;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserPointsTransaction;
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
	 */
	public ProducerPointsBalanceDTO getCurrentBalance(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		Integer balance = userPointsTransactionRepository.findLatestBalance(user.getId());
		if (balance == null) {
			balance = 0;
		}

		return new ProducerPointsBalanceDTO(user.getId(), user.getFullName(), balance);
	}

	/**
	 * Lista o histórico de transações de pontos do produtor (usuário autenticado).
	 */
	public List<ProducerPointsTransactionDTO> getTransactions(Integer userId) {
		List<UserPointsTransaction> transactions = userPointsTransactionRepository
				.findByUserIdOrderByCreatedAtDesc(userId);

		return transactions.stream()
				.map(t -> new ProducerPointsTransactionDTO(t.getId(),
						t.getTransactionType() != null ? t.getTransactionType().getCode() : null,
						t.getSourceType() != null ? t.getSourceType().getCode() : null,
						t.getActivity() != null ? t.getActivity().getDescription() : null,
						t.getReward() != null ? t.getReward().getName() : null, t.getPoints(), t.getBalanceAfter(),
						t.getCreatedAt()))
				.collect(Collectors.toList());
	}
}
