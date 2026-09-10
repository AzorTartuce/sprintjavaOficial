package com.sprintjava.controller.dto;

import com.sprintjava.model.AnalysisJob;

import java.time.LocalDateTime;

public class AnalysisJobResponse {

    private Long id;
    private String status;
    private String sourceFilename;
    private LocalDateTime createdAt;
    private String errorDetail;
    private MeetingDetailResponse meeting;

    public AnalysisJobResponse(AnalysisJob job, MeetingDetailResponse meeting) {
        this.id = job.getId();
        this.status = job.getStatus();
        this.sourceFilename = job.getSourceFilename();
        this.createdAt = job.getCreatedAt();
        this.errorDetail = job.getErrorDetail();
        this.meeting = meeting;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public MeetingDetailResponse getMeeting() {
        return meeting;
    }
}
