package br.com.agrogame.agrogame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resposta da decisão de aprovação/rejeição")
public class ActivityDecisionResponseDTO {

    @Schema(description = "ID da UserActivity", example = "1")
    private Integer userActivityId;

    @Schema(description = "ID da Activity", example = "10")
    private Integer activityId;

    @Schema(description = "Nome da atividade", example = "Aplicação de adubo")
    private String activityName;

    @Schema(description = "Decisão tomada", example = "approved")
    private String decision;

    @Schema(description = "Novo status", example = "approved")
    private String newStatus;

    @Schema(description = "Data da decisão", example = "2025-12-03T16:45:30")
    private LocalDateTime decidedAt;

    @Schema(description = "Motivo/Notas", example = "Documentação OK")
    private String reason;

    public ActivityDecisionResponseDTO() {
    }

    public ActivityDecisionResponseDTO(Integer userActivityId, Integer activityId,
                                       String activityName, String decision, String newStatus,
                                       LocalDateTime decidedAt, String reason) {
        this.userActivityId = userActivityId;
        this.activityId = activityId;
        this.activityName = activityName;
        this.decision = decision;
        this.newStatus = newStatus;
        this.decidedAt = decidedAt;
        this.reason = reason;
    }

    public Integer getUserActivityId() {
        return userActivityId;
    }

    public void setUserActivityId(Integer userActivityId) {
        this.userActivityId = userActivityId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
