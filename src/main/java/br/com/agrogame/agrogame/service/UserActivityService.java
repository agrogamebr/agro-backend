package br.com.agrogame.agrogame.service;

import br.com.agrogame.agrogame.dto.UserActivityDetailDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActivityService {

	private final UserActivityRepository userActivityRepository;
	private final UserRepository userRepository;

	public UserActivityService(UserActivityRepository userActivityRepository, UserRepository userRepository) {
		this.userActivityRepository = userActivityRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public UserActivityDetailDTO getUserActivityDetail(String email, Integer userActivityId) {
		User producer = userRepository.findByEmail1(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("User Activity não encontrada"));

		if (!userActivity.getUser().getId().equals(producer.getId())) {
			throw new BusinessException("Atividade não pertence ao produtor logado");
		}

		return toDto(userActivity);
	}

	private UserActivityDetailDTO toDto(UserActivity ua) {
		UserActivityDetailDTO dto = new UserActivityDetailDTO();
		dto.setId(ua.getId());
		dto.setUserId(ua.getUser() != null ? ua.getUser().getId() : null);
		dto.setActivityId(ua.getActivity() != null ? ua.getActivity().getId() : null);
		dto.setStatusId(ua.getStatus() != null ? ua.getStatus().getId() : null);
		dto.setStartedAt(ua.getStartedAt());
		dto.setCompletedAt(ua.getCompletedAt());
		dto.setCreatedAt(ua.getCreatedAt());
		dto.setCreatedById(ua.getCreatedBy() != null ? ua.getCreatedBy().getId() : null);
		dto.setUpdatedAt(ua.getUpdatedAt());
		dto.setUpdatedById(ua.getUpdatedBy() != null ? ua.getUpdatedBy().getId() : null);
		dto.setFarmId(ua.getFarm() != null ? ua.getFarm().getId() : null);
		return dto;
	}
}
