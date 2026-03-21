package br.com.agrogame.agrogame.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;

import br.com.agrogame.agrogame.dto.NotificationEvent;
import br.com.agrogame.agrogame.exceptions.BusinessException;

@Service
public class NotificationPublisherService {

	private static final Logger log = LoggerFactory.getLogger(NotificationPublisherService.class);

	private final PubSubTemplate pubSubTemplate;
	private final ObjectMapper objectMapper;

	@Value("${agrogame.pubsub.email-topic:dev-notifications-email}")
	private String emailTopic;

	public NotificationPublisherService(PubSubTemplate pubSubTemplate, ObjectMapper objectMapper) {
		this.pubSubTemplate = pubSubTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * Publica um evento de notificação no tópico de e-mail.
	 *
	 * @param event  payload padronizado (eventType, recipientEmail,
	 *               templateVariables)
	 * @param userId opcional, usado apenas para log (pode ser null)
	 */
	public void publishNotification(NotificationEvent event, Integer userId) {
		try {
			String jsonPayload = objectMapper.writeValueAsString(event);
			CompletableFuture<String> future = pubSubTemplate.publish(emailTopic, jsonPayload);

			future.whenComplete((messageId, exception) -> {
				if (exception != null) {
					log.error("Falha ao publicar evento [{}]. UserId: [{}], Destino: [{}], Erro: {}",
							event.eventType(), userId, event.recipientEmail(), exception.getMessage(), exception);
				} else {
					log.info("Evento [{}] publicado no Pub/Sub. UserId: [{}], Destino: [{}], MessageID: [{}]",
							event.eventType(), userId, event.recipientEmail(), messageId);
				}
			});

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Falha ao serializar evento de notificação", e);
		} catch (IllegalArgumentException e) {
			throw new BusinessException("Tópico do Pub/Sub não configurado corretamente.");
		}
	}
}
