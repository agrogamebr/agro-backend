package br.com.agrogame.agrogame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackofficeEmployeeService {

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	public Page<BackofficeEmployeeSummaryDTO> listEmployees(User operator, Integer userId, String name,
			Integer userTypeId, Pageable pageable) {
		Integer companyId = operator.getCompany().getId();
		String nameLike = (name != null && !name.isBlank()) ? "%" + name + "%" : null;

		return userRepository.findEmployeeSummariesByFilters(companyId, userTypeId, userId, nameLike, pageable);
	}
}
