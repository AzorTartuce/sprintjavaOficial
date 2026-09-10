package com.sprintjava.controller.dto;

import com.sprintjava.model.User;

import java.time.LocalDateTime;

/** Representação pública do usuário — nunca expõe passwordHash. */
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String company;
    private String jobTitle;
    private String department;
    private String phone;
    private String city;
    private String state;
    private String linkedinUrl;
    private String bio;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.company = user.getCompany();
        this.jobTitle = user.getJobTitle();
        this.department = user.getDepartment();
        this.phone = user.getPhone();
        this.city = user.getCity();
        this.state = user.getState();
        this.linkedinUrl = user.getLinkedinUrl();
        this.bio = user.getBio();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getBio() {
        return bio;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
