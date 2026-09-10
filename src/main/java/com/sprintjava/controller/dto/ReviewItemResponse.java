package com.sprintjava.controller.dto;

import com.sprintjava.model.Finding;

import java.time.LocalDateTime;

public class ReviewItemResponse {

    private String axis;
    private String decision;
    private Finding finding;
    private LocalDateTime reviewedAt;

    public ReviewItemResponse() {
    }

    public ReviewItemResponse(String axis, String decision, Finding finding, LocalDateTime reviewedAt) {
        this.axis = axis;
        this.decision = decision;
        this.finding = finding;
        this.reviewedAt = reviewedAt;
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public Finding getFinding() {
        return finding;
    }

    public void setFinding(Finding finding) {
        this.finding = finding;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
