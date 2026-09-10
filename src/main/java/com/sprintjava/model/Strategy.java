package com.sprintjava.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Strategy {

    private Long id;
    private String key;
    private String label;
    private List<String> appliesTo;
    private boolean isBuiltin;
    private boolean custom;

    public Strategy() {
    }

    public Strategy(Long id, String key, String label, List<String> appliesTo, boolean isBuiltin, boolean custom) {
        this.id = id;
        this.key = key;
        this.label = label;
        this.appliesTo = appliesTo;
        this.isBuiltin = isBuiltin;
        this.custom = custom;
    }

    /** Uma estratégia se aplica a um finding_type se listar "*" ou o próprio tipo. */
    public boolean aplicaA(String findingType) {
        return appliesTo != null && (appliesTo.contains("*") || appliesTo.contains(findingType));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(List<String> appliesTo) {
        this.appliesTo = appliesTo;
    }

    @JsonProperty("is_builtin")
    public boolean isBuiltin() {
        return isBuiltin;
    }

    public void setBuiltin(boolean builtin) {
        isBuiltin = builtin;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
