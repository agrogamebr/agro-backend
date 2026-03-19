package br.com.agrogame.agrogame.dto;

import java.util.Map;

/**
 * Evento padrão a ser publicado no tópico do GCP Pub/Sub para disparar e-mails.
 */
public record NotificationEvent(String eventType, String recipientEmail, Map<String, Object> templateVariables) {
}
