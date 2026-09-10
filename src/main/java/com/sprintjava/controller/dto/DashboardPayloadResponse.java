package com.sprintjava.controller.dto;

import java.util.List;
import java.util.Map;

public class DashboardPayloadResponse {

    private String scope;
    private Long clientId;
    private List<DashboardFindingRowResponse> openQueue;
    private List<StrategyRankingRowResponse> strategyRanking;
    private List<DashboardFindingRowResponse> severeWithoutActiveAttempt;
    private Double avgResolutionSeconds;
    private Map<String, Long> statusCounts;
    private Map<String, Long> totals;
    private List<OpenSuggestionResponse> openSuggestions;

    public DashboardPayloadResponse(String scope, Long clientId, List<DashboardFindingRowResponse> openQueue,
                                     List<StrategyRankingRowResponse> strategyRanking,
                                     List<DashboardFindingRowResponse> severeWithoutActiveAttempt,
                                     Double avgResolutionSeconds, Map<String, Long> statusCounts,
                                     Map<String, Long> totals, List<OpenSuggestionResponse> openSuggestions) {
        this.scope = scope;
        this.clientId = clientId;
        this.openQueue = openQueue;
        this.strategyRanking = strategyRanking;
        this.severeWithoutActiveAttempt = severeWithoutActiveAttempt;
        this.avgResolutionSeconds = avgResolutionSeconds;
        this.statusCounts = statusCounts;
        this.totals = totals;
        this.openSuggestions = openSuggestions;
    }

    public String getScope() {
        return scope;
    }

    public Long getClientId() {
        return clientId;
    }

    public List<DashboardFindingRowResponse> getOpenQueue() {
        return openQueue;
    }

    public List<StrategyRankingRowResponse> getStrategyRanking() {
        return strategyRanking;
    }

    public List<DashboardFindingRowResponse> getSevereWithoutActiveAttempt() {
        return severeWithoutActiveAttempt;
    }

    public Double getAvgResolutionSeconds() {
        return avgResolutionSeconds;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public Map<String, Long> getTotals() {
        return totals;
    }

    public List<OpenSuggestionResponse> getOpenSuggestions() {
        return openSuggestions;
    }
}
