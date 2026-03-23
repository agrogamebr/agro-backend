package br.com.agrogame.agrogame.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.NotificationEvent;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import jakarta.transaction.Transactional;

@Service
public class PasswordRecoveryService {

	@Autowired
	private AuthCredentialRepository authCredentialRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private NotificationPublisherService notificationPublisherService;

	@Value("${agrogame.pubsub.email-topic:dev-notifications-email}")
	private String emailTopic;

	@Transactional
	public void forgotPassword(String email) {
		AuthCredential cred = authCredentialRepository.findByIdentifier(email)
				.orElseThrow(() -> new BusinessException("Usuário não encontrado"));

		var user = cred.getUser();
		var statusCode = user.getUserStatus().getCode();
		boolean podeRecuperarSenha = EnumUserStatus.ACTIVE.getCode().equals(statusCode)
				|| EnumUserStatus.APPROVED.getCode().equals(statusCode);

		if (!podeRecuperarSenha) {
			// se o usuário não estiver ativo ou aprovado, não gera senha temporária e nem
			// envia email
			return;
		}

		String rawTempPassword = generateTemporaryPassword();
		String encodedTempPassword = passwordEncoder.encode(rawTempPassword);

		cred.setTemporaryPasswordHash(encodedTempPassword);
		cred.setTemporaryPasswordExpiresAt(LocalDateTime.now().plusHours(2));
		authCredentialRepository.save(cred);

		Map<String, Object> vars = Map.of("emailUsuario", email, "senhaTemporaria", rawTempPassword);

		NotificationEvent evento = new NotificationEvent("RECUPERACAO_SENHA", email, vars);

		notificationPublisherService.publishNotification(evento, cred.getUser().getId());
	}

	private String generateTemporaryPassword() {
		SecureRandom random = new SecureRandom();
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	@Transactional
	public void updateToNewPassword(Long userId, String newPassword) {
		AuthCredential credential = authCredentialRepository.findByUserId(userId)
				.orElseThrow(() -> new BusinessException("Credenciais não encontradas para o usuário."));

		// 1) Encripta a nova senha e salva como principal
		credential.setPasswordHash(passwordEncoder.encode(newPassword));

		// 2) Invalida a senha temporária
		credential.setTemporaryPasswordHash(null);
		credential.setTemporaryPasswordExpiresAt(null);

		// 3) Reseta tentativas falhas (opcional, mas recomendado)
		credential.setFailedAttempts(0);

		authCredentialRepository.save(credential);
	}

}
