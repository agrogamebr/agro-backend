package br.com.agrogame.agrogame.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.model.UserDocumentType;
import br.com.agrogame.agrogame.repository.UserDocumentTypeRepository;

@Service
public class UserDocumentTypeService {

	@Autowired
	private UserDocumentTypeRepository userDocumentTypeRepository;

	public List<UserDocumentType> listAll() {
		return userDocumentTypeRepository.findAll();
	}
}

