package br.com.agrogame.agrogame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Payload para decisão de aprovação/rejeição de atividade")
public class ActivityDecisionRequestDTO {

    @NotBlank(message = "Decision é obrigatória")
    @Pattern(
        regexp = "(?i)approved|rejected",              // (?i) = case insensitive
        message = "Decision deve ser 'approved' ou 'rejected' (case-insensitive)"
    )
    @Schema(
        description = """
            Decisão do backoffice.
            Valores aceitos (case-insensitive):
            - approved  → aprovar a atividade
            - rejected  → rejeitar a atividade
            """,
        example = "approved",
        allowableValues = {"approved", "rejected", "APPROVED", "REJECTED"}
    )
    private String decision;

    @Schema(
        description = """
            Motivo da decisão.
            Obrigatório e relevante principalmente quando decision = rejected.
            Ignorado na aprovação, mas pode ser enviado.
            """,
        example = "Documentação incompleta ou ilegível"
    )
    private String reason;

    public ActivityDecisionRequestDTO() {
    }

    public ActivityDecisionRequestDTO(String decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
