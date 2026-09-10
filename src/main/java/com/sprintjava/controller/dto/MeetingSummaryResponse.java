package com.sprintjava.controller.dto;

import com.sprintjava.model.Meeting;

import java.time.LocalDateTime;

public class MeetingSummaryResponse {

    private Long id;
    private String sourceFilename;
    private LocalDateTime createdAt;
    private String conta;
    private String status;

    public MeetingSummaryResponse(Meeting meeting, String contaLabel) {
        this.id = meeting.getId();
        this.sourceFilename = meeting.getSourceFilename();
        this.createdAt = meeting.getCreatedAt();
        this.conta = contaLabel;
        this.status = meeting.possuiAnaliseCompleta() ? "concluida" : "pendente";
    }

    public Long getId() {
        return id;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getConta() {
        return conta;
    }

    public String getStatus() {
        return status;
    }
}
