package com.sprintjava.controller.dto;

import com.sprintjava.model.Finding;
import com.sprintjava.model.Suggestion;

public class SuggestionDecisionResponse {

    private Suggestion suggestion;
    private Finding finding;

    public SuggestionDecisionResponse(Suggestion suggestion, Finding finding) {
        this.suggestion = suggestion;
        this.finding = finding;
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public Finding getFinding() {
        return finding;
    }
}
