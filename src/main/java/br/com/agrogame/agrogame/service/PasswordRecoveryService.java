package br.com.agrogame.agrogame.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;

import br.com.agrogame.agrogame.dto.NotificationEvent;
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
	private PubSubTemplate pubSubTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${agrogame.pubsub.email-topic:dev-notifications-email}")
	private String emailTopic;

	private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);

	@Transactional
	public void forgotPassword(String email) {
		AuthCredential cred = authCredentialRepository.findByIdentifier(email)
				.orElseThrow(() -> new BusinessException("Usuário não encontrado"));

		// 1) Gera senha temporária
		String rawTempPassword = generateTemporaryPassword();
		String encodedTempPassword = passwordEncoder.encode(rawTempPassword);

		// 2) Persiste hash + expiração
		cred.setTemporaryPasswordHash(encodedTempPassword);
		cred.setTemporaryPasswordExpiresAt(LocalDateTime.now().plusHours(2));
		authCredentialRepository.save(cred);

		// 4) Prepara as variáveis para o template do microsserviço
		// *Alinhamento: O microsserviço precisa esperar 'senhaTemporaria' em vez de
		// 'linkRecuperacao'
		Map<String, Object> vars = Map.of("emailUsuario", email, "senhaTemporaria",
				rawTempPassword);

		// 5) Cria o Evento Padronizado do contrato
		NotificationEvent evento = new NotificationEvent("RECUPERACAO_SENHA", email, vars);

		try {
			String jsonPayload = objectMapper.writeValueAsString(evento);
			CompletableFuture<String> future = pubSubTemplate.publish(emailTopic, jsonPayload);

			future.whenComplete((messageId, exception) -> {
				if (exception != null) {
					log.error("Falha ao publicar evento de recuperação de senha. UserId: [{}], Destino: [{}], Erro: {}",
							cred.getUser().getId(), email, exception.getMessage());
				} else {
					log.info(
							"Evento [RECUPERACAO_SENHA] publicado no Pub/Sub. UserId: [{}], Destino: [{}], MessageID: [{}]",
							cred.getUser().getId(), email, messageId);
				}
			});

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Falha ao serializar evento de notificação", e);
		} catch (IllegalArgumentException e) {
			throw new BusinessException("Tópico do Pub/Sub não configurado corretamente.");
		}
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
