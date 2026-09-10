package com.sprintjava.controller.dto;

public class StrategyRankingRowResponse {

    private String strategyKey;
    private String strategyLabel;
    private long worked;
    private long failed;
    private long partial;
    private long inProgress;
    private long total;
    private Double taxaSucesso;
    private boolean lowSample;

    public StrategyRankingRowResponse(String strategyKey, String strategyLabel, long worked, long failed,
                                       long partial, long inProgress) {
        this.strategyKey = strategyKey;
        this.strategyLabel = strategyLabel;
        this.worked = worked;
        this.failed = failed;
        this.partial = partial;
        this.inProgress = inProgress;
        this.total = worked + failed + partial + inProgress;
        long concluidas = worked + failed + partial;
        this.taxaSucesso = concluidas > 0 ? (double) worked / concluidas : null;
        this.lowSample = total < 5;
    }

    public String getStrategyKey() {
        return strategyKey;
    }

    public String getStrategyLabel() {
        return strategyLabel;
    }

    public long getWorked() {
        return worked;
    }

    public long getFailed() {
        return failed;
    }

    public long getPartial() {
        return partial;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getTotal() {
        return total;
    }

    public Double getTaxaSucesso() {
        return taxaSucesso;
    }

    public boolean isLowSample() {
        return lowSample;
    }
}
