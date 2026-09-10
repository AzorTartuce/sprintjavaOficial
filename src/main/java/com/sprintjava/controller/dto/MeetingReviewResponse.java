package com.sprintjava.controller.dto;

import java.util.ArrayList;
import java.util.List;

public class MeetingReviewResponse {

    private Long meetingId;
    private List<ReviewItemResponse> items = new ArrayList<>();

    public MeetingReviewResponse() {
    }

    public MeetingReviewResponse(Long meetingId, List<ReviewItemResponse> items) {
        this.meetingId = meetingId;
        this.items = items;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public List<ReviewItemResponse> getItems() {
        return items;
    }

    public void setItems(List<ReviewItemResponse> items) {
        this.items = items;
    }
}
