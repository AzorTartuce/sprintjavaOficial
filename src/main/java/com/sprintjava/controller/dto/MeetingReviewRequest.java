package com.sprintjava.controller.dto;

import java.util.List;

public class MeetingReviewRequest {

    private List<ReviewItemRequest> decisions;

    public List<ReviewItemRequest> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<ReviewItemRequest> decisions) {
        this.decisions = decisions;
    }
}
