package br.com.agrogame.agrogame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.BackofficeEmployeeSummaryDTO;
import br.com.agrogame.agrogame.dto.BackofficeProducerSummaryDTO;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class BackofficeEmployeeService {

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	public Page<BackofficeEmployeeSummaryDTO> listEmployees(User operator, Integer userId, String name,
			Integer userTypeId, String cpf, Integer statusId, Pageable pageable) {
		Integer companyId = operator.getCompany().getId();
		String nameLike = (name != null && !name.isBlank()) ? "%" + name + "%" : null;
		String cpfLike = (cpf != null && !cpf.isBlank()) ? "%" + cpf + "%" : null;

		return userRepository.findEmployeeSummariesByFilters(companyId, userTypeId, userId, nameLike, cpfLike, statusId,
				pageable);
	}

	@Transactional(readOnly = true)
	public Page<BackofficeProducerSummaryDTO> listProducers(User operator, String name, String cpf, Integer statusId,
			Pageable pageable) {

		Integer companyId = operator.getCompany().getId();
		String nameLike = (name != null && !name.isBlank()) ? "%" + name.toLowerCase() + "%" : null;
		String cpfLike = (cpf != null && !cpf.isBlank()) ? "%" + cpf + "%" : null;

		Page<BackofficeProducerSummaryDTO> page = userRepository.listProducers(companyId, nameLike, cpfLike, statusId,
				pageable);

		return page;
	}

}
