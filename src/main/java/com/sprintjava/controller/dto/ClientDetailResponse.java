package com.sprintjava.controller.dto;

import com.sprintjava.model.Client;
import com.sprintjava.model.Meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ClientDetailResponse {

    private Long id;
    private String name;
    private String segment;
    private String companySize;
    private String website;
    private String city;
    private String state;
    private String contactName;
    private String contactRole;
    private String contactEmail;
    private String contactPhone;
    private String owner;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private List<MeetingSummaryResponse> meetings;

    public ClientDetailResponse(Client client, List<Meeting> meetings) {
        this.id = client.getId();
        this.name = client.getName();
        this.segment = client.getSegment();
        this.companySize = client.getCompanySize();
        this.website = client.getWebsite();
        this.city = client.getCity();
        this.state = client.getState();
        this.contactName = client.getContactName();
        this.contactRole = client.getContactRole();
        this.contactEmail = client.getContactEmail();
        this.contactPhone = client.getContactPhone();
        this.owner = client.getOwner();
        this.status = client.getStatus();
        this.notes = client.getNotes();
        this.createdAt = client.getCreatedAt();
        this.meetings = meetings.stream()
                .map(meeting -> new MeetingSummaryResponse(meeting, client.getName()))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSegment() {
        return segment;
    }

    public String getCompanySize() {
        return companySize;
    }

    public String getWebsite() {
        return website;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactRole() {
        return contactRole;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getOwner() {
        return owner;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<MeetingSummaryResponse> getMeetings() {
        return meetings;
    }
}
