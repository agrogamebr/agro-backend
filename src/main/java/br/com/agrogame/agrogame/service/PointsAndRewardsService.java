package br.com.agrogame.agrogame.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Activity;
import br.com.agrogame.agrogame.model.ActivityReward;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserPointTransactionSourceType;
import br.com.agrogame.agrogame.model.UserPointTransactionType;
import br.com.agrogame.agrogame.model.UserPointsTransaction;
import br.com.agrogame.agrogame.model.UserReward;
import br.com.agrogame.agrogame.model.UserRewardHistory;
import br.com.agrogame.agrogame.model.UserRewardStatus;
import br.com.agrogame.agrogame.repository.ActivityRewardRepository;
import br.com.agrogame.agrogame.repository.UserPointTransactionSourceTypeRepository;
import br.com.agrogame.agrogame.repository.UserPointTransactionTypeRepository;
import br.com.agrogame.agrogame.repository.UserPointsTransactionRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.repository.UserRewardHistoryRepository;
import br.com.agrogame.agrogame.repository.UserRewardRepository;
import br.com.agrogame.agrogame.repository.UserRewardStatusRepository;

@Service
@Transactional
public class PointsAndRewardsService {

	private static final Logger logger = LoggerFactory.getLogger(PointsAndRewardsService.class);

	private final UserPointsTransactionRepository userPointsTransactionRepository;
	private final UserPointTransactionTypeRepository userPointTransactionTypeRepository;
	private final UserPointTransactionSourceTypeRepository userPointTransactionSourceTypeRepository;
	private final UserRewardRepository userRewardRepository;
	private final UserRewardHistoryRepository userRewardHistoryRepository;
	private final UserRewardStatusRepository userRewardStatusRepository;
	private final ActivityRewardRepository activityRewardRepository;
	private final UserRepository userRepository;

	public PointsAndRewardsService(UserPointsTransactionRepository userPointsTransactionRepository,
			UserPointTransactionTypeRepository userPointTransactionTypeRepository,
			UserPointTransactionSourceTypeRepository userPointTransactionSourceTypeRepository,
			UserRewardRepository userRewardRepository, UserRewardHistoryRepository userRewardHistoryRepository,
			UserRewardStatusRepository userRewardStatusRepository, ActivityRewardRepository activityRewardRepository, UserRepository userRepository) {
		this.userPointsTransactionRepository = userPointsTransactionRepository;
		this.userPointTransactionTypeRepository = userPointTransactionTypeRepository;
		this.userPointTransactionSourceTypeRepository = userPointTransactionSourceTypeRepository;
		this.userRewardRepository = userRewardRepository;
		this.userRewardHistoryRepository = userRewardHistoryRepository;
		this.userRewardStatusRepository = userRewardStatusRepository;
		this.activityRewardRepository = activityRewardRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Credita pontos ao usuário quando uma atividade é aprovada. Cria transação de
	 * pontos e recompensas vinculadas.
	 */
	public void creditOnActivityApproval(UserActivity userActivity, User backofficeUser) {
		try {
			logger.info("Iniciando crédito de pontos para UserActivity ID: {}, Usuário: {}", userActivity.getId(),
					userActivity.getUser().getId());

			// 1. Validar dados obrigatórios
			if (userActivity.getUser() == null || userActivity.getActivity() == null) {
				throw new BusinessException("UserActivity não possui usuário ou atividade vinculados");
			}

			// 2. Obter dados necessários
			User producer = userActivity.getUser();
			Activity activity = userActivity.getActivity();
			Integer points = activity.getPoints();

			if (points == null || points <= 0) {
				logger.warn("Atividade ID: {} não possui pontos válidos", activity.getId());
				throw new BusinessException("Atividade não possui pontuação configurada");
			}

			// 3. Obter tipos de transação
			UserPointTransactionType earnType = userPointTransactionTypeRepository.findByCode("earn")
					.orElseThrow(() -> new ResourceNotFoundException("Tipo de transação 'earn' não configurado"));

			UserPointTransactionSourceType activityApprovalSource = userPointTransactionSourceTypeRepository
					.findByCode("activity")
					.orElseThrow(() -> new ResourceNotFoundException("Fonte de transação 'activity' não configurada"));

			// 4. Calcular novo saldo
			Integer currentBalance = userPointsTransactionRepository.findLatestBalance(producer.getId());
			if (currentBalance == null) {
				currentBalance = 0;
			}
			Integer newBalance = currentBalance + points;
			
			producer.setPointsBalance(newBalance); // Atualiza o objeto em memória
			userRepository.save(producer); // Persiste a atualização do saldo no banco

			// 6. Buscar recompensas vinculadas à atividade (antes da transação, para
			// decidir reward_id)
			List<ActivityReward> activityRewards = activityRewardRepository.findByActivityId(activity.getId());

			// 5. Criar transação de pontos
			UserPointsTransaction transaction = new UserPointsTransaction();
			transaction.setUser(producer);
			transaction.setTransactionType(earnType);
			transaction.setSourceType(activityApprovalSource);
			transaction.setActivity(activity);
			transaction.setPoints(points);
			transaction.setBalanceAfter(newBalance);
			transaction.setCreatedBy(backofficeUser);
			if (activityRewards != null && activityRewards.size() == 1) {
			    transaction.setReward(activityRewards.get(0).getReward());
			}

			userPointsTransactionRepository.save(transaction);

			logger.info("Transação de pontos criada: {} pontos para usuário {}, novo saldo: {}", points,
					producer.getId(), newBalance);

			// 7. Processar recompensas (UserReward + histórico)
			if (activityRewards != null && !activityRewards.isEmpty()) {
				logger.info("Processando {} recompensas para atividade ID: {}", activityRewards.size(),
						activity.getId());

				UserRewardStatus grantedStatus = userRewardStatusRepository.findByCode("granted").orElseThrow(
						() -> new ResourceNotFoundException("Status de recompensa 'granted' não configurado"));

				for (ActivityReward activityReward : activityRewards) {
					// Criar user_reward
					UserReward userReward = new UserReward();
					userReward.setUser(producer);
					userReward.setReward(activityReward.getReward());
					userReward.setUserRewardStatus(grantedStatus);
					userReward.setCreatedBy(backofficeUser);
					UserReward savedReward = userRewardRepository.save(userReward);

					// Registrar histórico
					UserRewardHistory history = new UserRewardHistory();
					history.setUserReward(savedReward);
					history.setNewStatus(grantedStatus);
					history.setCreatedBy(backofficeUser);
					history.setChangedBy(backofficeUser);
					history.setNotes("Recompensa atribuída pela aprovação da atividade ID: " + activity.getId());
					userRewardHistoryRepository.save(history);

					logger.info("Recompensa ID: {} atribuída ao usuário {}", activityReward.getReward().getId(),
							producer.getId());
				}
			}

			logger.info("Crédito de pontos finalizado com sucesso para UserActivity ID: {}", userActivity.getId());

		} catch (ResourceNotFoundException e) {
			logger.error("Erro de configuração ao creditar pontos: {}", e.getMessage());
			throw e;
		} catch (BusinessException e) {
			logger.error("Erro de negócio ao creditar pontos: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Erro inesperado ao creditar pontos para UserActivity ID: {}", userActivity.getId(), e);
			throw new BusinessException("Erro ao processar crédito de pontos: " + e.getMessage());
		}
	}

}
