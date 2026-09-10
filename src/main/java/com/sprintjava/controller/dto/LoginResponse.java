package com.sprintjava.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprintjava.model.User;

public class LoginResponse {

    @JsonProperty("access_token")
    private String accessToken;
    private UserResponse user;

    public LoginResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.user = new UserResponse(user);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
