package br.com.agrogame.agrogame.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.model.User;

@Service
public class AccessControlService {

	public void validateBackofficeUser(User user) {
		if (user.getCompany() == null) {
			throw new BusinessException("Usuário backoffice não está vinculado a uma empresa");
		}

		Integer typeId = user.getUserType().getId();
		if (!isBackofficeUserType(typeId)) {
			throw new AccessDeniedException(
					"Apenas usuários com permissão de backoffice (tipos 1,2,3,6) podem acessar");
		}
	}
	
	public void validateAcessUser(User user) {
		if (!isBackofficeUserType(user.getId())) {
			throw new BusinessException("Você não tem acesso ao recurso solicitado.");
		}
	}

	private boolean isBackofficeUserType(Integer userTypeId) {
		return userTypeId == 1 || userTypeId == 2 || userTypeId == 3 || userTypeId == 6;
	}
}
