package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.BackofficeWorkerDTO;
import br.com.agrogame.agrogame.dto.WorkerCreateDTO;
import br.com.agrogame.agrogame.dto.WorkerDetailDTO;
import br.com.agrogame.agrogame.dto.WorkerUpdateDTO;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.BadRequestException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.DuplicateResourceException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.model.WorkerFarmAssignment;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.repository.UserStatusRepository;
import br.com.agrogame.agrogame.repository.UserTypeRepository;
import br.com.agrogame.agrogame.repository.WorkerFarmAssignmentRepository;
import br.com.agrogame.agrogame.repository.WorkerRepository;

@Service
public class WorkerService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserTypeRepository userTypeRepository;

	@Autowired
	private UserStatusRepository userStatusRepository;

	@Autowired
	private WorkerRepository workerRepository;

	@Autowired
	private WorkerFarmAssignmentRepository workerFarmAssignmentRepository;

	@Autowired
	private AuthCredentialRepository authCredentialRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private FarmRepository farmRepository;

	@Transactional(readOnly = true)
	public List<WorkerDetailDTO> listWorkers(String producerEmail, Integer farmIdFilter, Integer workerIdFilter) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Usuário autenticado não é produtor rural");
		}

		if (farmIdFilter != null) {
			Farm farm = farmRepository.findById(farmIdFilter)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(producer.getId())) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
		}

		return workerRepository.findWorkersByProducer(producer.getId(), farmIdFilter, workerIdFilter);
	}

	@Transactional
	public WorkerDetailDTO registerWorker(String producerEmail, WorkerCreateDTO dto) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Apenas produtores podem cadastrar workers");
		}

		// 1) Validar formato e unicidade do email
		if (!validationService.isValidEmailFormat(dto.getEmail())) {
			throw new BadRequestException("Formato de e-mail inválido");
		}
		if (validationService.emailAlreadyExists(dto.getEmail())) {
			throw new DuplicateResourceException("E-mail já cadastrado");
		}

		// 2) Validar senha
		validatePassword(dto.getPassword());

		// 3) Validar se a fazenda informada pertence ao produtor logado
		Farm farm = farmRepository.findById(dto.getFarmId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
		if (!farm.getOwner().getId().equals(producer.getId())) {
			throw new BusinessException("Fazenda não pertence ao produtor logado");
		}

		// 4) Tipo e status do worker
		UserType workerType = userTypeRepository.findByCode(EnumUserType.WORKER.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário WORKER não encontrado"));

		UserStatus activeStatus = userStatusRepository.findByCode(EnumUserStatus.APPROVED.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status ativo não encontrado"));

		// 5) Montar entidade User
		User worker = new User();
		worker.setFullName(dto.getFullname());
		worker.setEmail1(dto.getEmail());
		worker.setPhone(dto.getPhone());
		worker.setUserType(workerType);
		worker.setUserStatus(activeStatus);
		worker.setCreatedAt(LocalDateTime.now());
		worker.setPointsBalance(0);

		worker.setAddress(dto.getAddress());
		worker.setNumber(dto.getNumber());
		worker.setCity(dto.getCity());
		worker.setState(dto.getState());
		worker.setZipcode(dto.getZipcode());
		worker.setCompany(producer.getCompany());

		String[] parts = dto.getFullname().trim().split("\\s+", 2);
		worker.setFirstName(parts[0]);
		worker.setLastName(parts.length > 1 ? parts[1] : "");

		User savedWorker = userRepository.save(worker);

		// 6) credencial em auth_credentials
		AuthCredential credential = new AuthCredential();
		credential.setUser(savedWorker);
		credential.setProvider("email");
		credential.setIdentifier(dto.getEmail());
		credential.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
		credential.setIsActive(true);
		credential.setFailedAttempts(0);
		credential.setCreatedAt(LocalDateTime.now());

		authCredentialRepository.save(credential);

		// 7) vincular worker à farm (nova tabela)
		createWorkerFarmAssignment(producer, savedWorker.getId(), dto.getFarmId());

		return toDetailDTO(savedWorker);
	}

	/**
	 * Endpoint antigo de associar unidade produtiva. Com a nova regra worker ↔
	 * farm, esse método não deve mais ser usado para vincular UP; pode ser mantido
	 * temporariamente para compatibilidade, ou deprecado/removido quando o front
	 * parar de chamar.
	 */
	@Transactional
	public void assignUnit(String loggedEmail, Integer workerId, Integer productionUnitId) {
		// Regra nova: worker vê todas as UPs da farm.
		// Aqui podemos:
		// - lançar exceção explicando que o vínculo agora é por farm; ou
		// - simplesmente ignorar se você quiser manter compatibilidade.
		throw new BusinessException("Vínculo de worker agora é feito por fazenda, não por unidade produtiva.");
	}

	// EDITAR worker (produtor ou o próprio worker)
	@Transactional
	public WorkerDetailDTO updateWorker(String loggedEmail, Integer workerId, WorkerUpdateDTO dto) {
		User loggedUser = findUserByEmailWithType(loggedEmail);
		User worker = userRepository.findById(workerId)
				.orElseThrow(() -> new ResourceNotFoundException("Worker não encontrado"));

		boolean isSelf = loggedUser.getId().equals(workerId);
		boolean isProducerOwner = isProducer(loggedUser)
				&& workerRepository.existsWorkerBelongsToProducer(loggedEmail, workerId);

		if (!isSelf && !isProducerOwner) {
			throw new BusinessException("Você não tem permissão para editar este worker");
		}

		if (dto.getFullname() != null && !dto.getFullname().isBlank()) {
			worker.setFullName(dto.getFullname());
			String[] parts = dto.getFullname().trim().split("\\s+", 2);
			worker.setFirstName(parts[0]);
			worker.setLastName(parts.length > 1 ? parts[1] : "");
		}

		if (dto.getPhone() != null) {
			worker.setPhone(dto.getPhone());
		}
		if (dto.getAddress() != null) {
			worker.setAddress(dto.getAddress());
		}
		if (dto.getNumber() != null) {
			worker.setNumber(dto.getNumber());
		}
		if (dto.getCity() != null) {
			worker.setCity(dto.getCity());
		}
		if (dto.getState() != null) {
			worker.setState(dto.getState());
		}
		if (dto.getZipcode() != null) {
			worker.setZipcode(dto.getZipcode());
		}

		worker.setUpdatedAt(LocalDateTime.now());
		User saved = userRepository.save(worker);

		// Se no futuro permitir trocar a farm do worker aqui,
		// dá pra adicionar um campo farmId no WorkerUpdateDTO
		// e chamar createWorkerFarmAssignment(loggedUser, workerId, dto.getFarmId()).

		return toDetailDTO(saved);
	}

	// INATIVAR worker
	@Transactional
	public void deactivateWorker(String producerEmail, Integer workerId) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Apenas produtores podem inativar workers");
		}

		if (!workerRepository.existsWorkerBelongsToProducer(producerEmail, workerId)) {
			throw new BusinessException("Worker não pertence ao produtor");
		}

		User worker = userRepository.findById(workerId)
				.orElseThrow(() -> new ResourceNotFoundException("Worker não encontrado"));

		UserStatus inactiveStatus = userStatusRepository.findByCode(EnumUserStatus.INACTIVE.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status INACTIVE não encontrado"));

		worker.setUserStatus(inactiveStatus);
		worker.setUpdatedAt(LocalDateTime.now());
		userRepository.save(worker);

		authCredentialRepository.findByProviderAndIdentifier("email", worker.getEmail1()).ifPresent(cred -> {
			cred.setIsActive(false);
			cred.setUpdatedAt(LocalDateTime.now());
			authCredentialRepository.save(cred);
		});
	}

	// AUXILIARES

	private User findUserByEmailWithType(String email) {
		return userRepository.findByEmail1(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
	}

	private boolean isProducer(User user) {
		return user.getUserType() != null && EnumUserType.PRODUCER.getCode().equals(user.getUserType().getCode());
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8) {
			throw new BadRequestException("Senha deve ter pelo menos 8 caracteres");
		}
		if (!password.matches(".*[A-Z].*")) {
			throw new BadRequestException("Senha deve conter pelo menos uma letra maiúscula");
		}
		if (!password.matches(".*[^A-Za-z0-9].*")) {
			throw new BadRequestException("Senha deve conter pelo menos um caractere especial");
		}
	}

	private WorkerDetailDTO toDetailDTO(User worker) {
		WorkerDetailDTO dto = new WorkerDetailDTO();
		dto.setId(worker.getId());
		dto.setFullname(worker.getFullName());
		dto.setEmail(worker.getEmail1());
		dto.setPhone(worker.getPhone());
		dto.setProfilePictureUrl(worker.getProfilePictureUrl());
		dto.setZipcode(worker.getZipcode());
		dto.setNumber(worker.getNumber());
		return dto;
	}

	@Transactional
	protected void createWorkerFarmAssignment(User producer, Integer workerId, Integer farmId) {
		Farm farm = farmRepository.findById(farmId)
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		if (!farm.getOwner().getId().equals(producer.getId())) {
			throw new BusinessException("Fazenda não pertence ao produtor logado");
		}

		workerFarmAssignmentRepository.findByWorkerIdAndIsActiveTrue(workerId.longValue()).ifPresent(active -> {
			active.setIsActive(false);
			active.setUpdatedBy(producer.getId().longValue());
			workerFarmAssignmentRepository.save(active);
		});

		WorkerFarmAssignment assignment = new WorkerFarmAssignment();
		assignment.setWorkerId(workerId.longValue());
		assignment.setFarmId(farm.getId().longValue());
		assignment.setIsActive(true);
		assignment.setUpdatedBy(producer.getId().longValue());

		workerFarmAssignmentRepository.save(assignment);
	}

	@Transactional(readOnly = true)
	public Page<BackofficeWorkerDTO> listWorkersByOwnerForBackoffice(String loggedEmail, Integer ownerId,
			Pageable pageable) {

		User operator = userRepository.findByEmail1(loggedEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

		boolean isSuperAdmin = operator.getUserType().getId() == 10;
		Integer companyId = isSuperAdmin ? null : operator.getCompany().getId();

		Page<User> page = workerRepository.findWorkersByOwnerAndCompany(ownerId, companyId, pageable);

		return page.map(this::toBackofficeWorkerDTO);
	}

	private BackofficeWorkerDTO toBackofficeWorkerDTO(User worker) {
		BackofficeWorkerDTO dto = new BackofficeWorkerDTO();
		dto.setId(worker.getId());
		dto.setFullname(worker.getFullName());
		dto.setEmail(worker.getEmail1());

		// Agora pega a farm ativa via worker_farm_assignments
		workerFarmAssignmentRepository.findByWorkerIdAndIsActiveTrue(worker.getId().longValue())
				.ifPresent(assignment -> {
					farmRepository.findById(assignment.getFarmId().intValue()).ifPresent(farm -> {
						dto.setFarmId(farm.getId());
						dto.setFarmName(farm.getName());
					});
				});

		return dto;
	}
}