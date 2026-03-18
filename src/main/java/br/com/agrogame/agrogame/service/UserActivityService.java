package br.com.agrogame.agrogame.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.UserActivityDetailDTO;
import br.com.agrogame.agrogame.dto.UserActivityReviewDTO;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserActivity;
import br.com.agrogame.agrogame.model.UserActivityReview;
import br.com.agrogame.agrogame.model.UserActivityStatus;
import br.com.agrogame.agrogame.repository.UserActivityRepository;
import br.com.agrogame.agrogame.repository.UserActivityReviewRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class UserActivityService {

	private final UserActivityRepository userActivityRepository;
	private final UserRepository userRepository;
	private final UserActivityReviewRepository userActivityReviewRepository;

	public UserActivityService(UserActivityRepository userActivityRepository, UserRepository userRepository,
			UserActivityReviewRepository userActivityReviewRepository) {
		this.userActivityRepository = userActivityRepository;
		this.userRepository = userRepository;
		this.userActivityReviewRepository = userActivityReviewRepository;
	}

	@Transactional(readOnly = true)
	public UserActivityDetailDTO getUserActivityDetail(String email, Integer userActivityId) {
		User producer = userRepository.findByEmail1(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

		UserActivity userActivity = userActivityRepository.findById(userActivityId)
				.orElseThrow(() -> new ResourceNotFoundException("User Activity não encontrada"));

		UserActivityStatus status = userActivity.getStatus();
		if (status != null) {
			status.getName();
			status.getCode();
		}

		List<UserActivityReview> reviews = userActivityReviewRepository
				.findByUserActivityIdOrderByReviewedAtDesc(userActivityId);

		if (!userActivity.getUser().getId().equals(producer.getId())) {
			throw new BusinessException("Atividade não pertence ao produtor logado");
		}

		return toDto(userActivity, reviews);
	}

	private UserActivityDetailDTO toDto(UserActivity ua, List<UserActivityReview> reviews) {
		UserActivityDetailDTO dto = new UserActivityDetailDTO();
		dto.setId(ua.getId());
		dto.setUserId(ua.getUser() != null ? ua.getUser().getId() : null);
		dto.setActivityId(ua.getActivity() != null ? ua.getActivity().getId() : null);

		if (ua.getStatus() != null) {
			dto.setStatusId(ua.getStatus().getId());
			dto.setStatusName(ua.getStatus().getName());
			dto.setStatusCode(ua.getStatus().getCode());
		}

		dto.setStartedAt(ua.getStartedAt());
		dto.setCompletedAt(ua.getCompletedAt());
		dto.setCreatedAt(ua.getCreatedAt());
		dto.setCreatedById(ua.getCreatedBy() != null ? ua.getCreatedBy().getId() : null);
		dto.setUpdatedAt(ua.getUpdatedAt());
		dto.setUpdatedById(ua.getUpdatedBy() != null ? ua.getUpdatedBy().getId() : null);
		dto.setFarmId(ua.getFarm() != null ? ua.getFarm().getId() : null);

		List<UserActivityReviewDTO> reviewDtos = reviews.stream().map(r -> {
			UserActivityReviewDTO rdto = new UserActivityReviewDTO();
			rdto.setId(r.getId());
			rdto.setStatusCode(r.getReviewStatus() != null ? r.getReviewStatus().getCode() : null);
			rdto.setStatusName(r.getReviewStatus() != null ? r.getReviewStatus().getName() : null);
			rdto.setReviewerId(r.getReviewer() != null ? r.getReviewer().getId() : null);
			rdto.setReviewerName(r.getReviewer() != null ? r.getReviewer().getFullName() : null);
			rdto.setNotes(r.getReviewNotes());
			rdto.setReviewedAt(r.getReviewedAt());
			return rdto;
		}).toList();

		dto.setReviews(reviewDtos);

		return dto;
	}

}
