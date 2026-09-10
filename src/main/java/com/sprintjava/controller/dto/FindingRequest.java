package com.sprintjava.controller.dto;

/** Payload de atualização de finding — campos null não são alterados. */
public class FindingRequest {

    private String title;
    private String severityHint;
    private String status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverityHint() {
        return severityHint;
    }

    public void setSeverityHint(String severityHint) {
        this.severityHint = severityHint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
