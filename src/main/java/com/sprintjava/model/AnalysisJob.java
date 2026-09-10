package com.sprintjava.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class AnalysisJob {

    public static final String STATUS_QUEUED = "queued";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_FAILED = "failed";

    private static final Set<String> STATUS_RECUPERAVEIS = Set.of(STATUS_QUEUED, STATUS_RUNNING);

    private Long id;
    private Long userId;
    private Long clientId;
    private String sourceFilename;
    private String inputText;
    private String status;
    private String errorDetail;
    private Long meetingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AnalysisJob() {
    }

    public AnalysisJob(Long userId, Long clientId, String sourceFilename, String inputText) {
        this.userId = userId;
        this.clientId = clientId;
        this.sourceFilename = sourceFilename;
        this.inputText = inputText;
        this.status = STATUS_QUEUED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public AnalysisJob(Long id, Long userId, Long clientId, String sourceFilename, String inputText,
                        String status, String errorDetail, Long meetingId, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.clientId = clientId;
        this.sourceFilename = sourceFilename;
        this.inputText = inputText;
        this.status = status;
        this.errorDetail = errorDetail;
        this.meetingId = meetingId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    
    public void iniciarProcessamento() {
        if (!STATUS_RECUPERAVEIS.contains(status)) {
            throw new IllegalStateException(
                    "Só é possível iniciar processamento a partir de queued/running, status atual: " + status);
        }
        this.status = STATUS_RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    
    public void concluirComSucesso(Long meetingId) {
        if (!STATUS_RUNNING.equals(status)) {
            throw new IllegalStateException("Só é possível concluir um job em execução (running).");
        }
        this.meetingId = meetingId;
        this.status = STATUS_DONE;
        this.errorDetail = null;
        this.updatedAt = LocalDateTime.now();
    }

    
    public void falhar(String motivo) {
        this.status = STATUS_FAILED;
        this.errorDetail = motivo;
        this.updatedAt = LocalDateTime.now();
    }

   
    public boolean podeSerRecuperado() {
        return STATUS_RECUPERAVEIS.contains(status);
    }

    
    public long duracaoProcessamentoSegundos() {
        if (createdAt == null || updatedAt == null) {
            return 0L;
        }
        return Duration.between(createdAt, updatedAt).getSeconds();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalysisJob)) return false;
        AnalysisJob that = (AnalysisJob) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AnalysisJob{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", status='" + status + '\'' +
                ", meetingId=" + meetingId +
                ", createdAt=" + createdAt +
                '}';
    }
}
