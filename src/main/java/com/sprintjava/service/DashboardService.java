package com.sprintjava.service;

import com.sprintjava.controller.dto.DashboardFindingRowResponse;
import com.sprintjava.controller.dto.DashboardPayloadResponse;
import com.sprintjava.controller.dto.OpenSuggestionResponse;
import com.sprintjava.controller.dto.StrategyRankingRowResponse;
import com.sprintjava.model.Finding;
import com.sprintjava.model.FindingAttempt;
import com.sprintjava.model.Suggestion;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Monta o DashboardPayload (ver docs/frontinformacoes.md, seção 7) a partir dos findings de um escopo. */
@Service
public class DashboardService {

    public DashboardPayloadResponse montar(String scope, Long clientId, List<Finding> findings,
                                            List<Suggestion> openSuggestions, Map<Long, Finding> findingsPorId) {
        List<DashboardFindingRowResponse> openQueue = findings.stream()
                .filter(f -> Finding.STATUS_AWAITING_STRATEGY.equals(f.getStatus())
                        || Finding.STATUS_IN_PROGRESS.equals(f.getStatus()))
                .sorted(Comparator.comparing(Finding::getOpenedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(DashboardFindingRowResponse::new)
                .collect(Collectors.toList());

        List<DashboardFindingRowResponse> severeSemTentativaAtiva = findings.stream()
                .filter(f -> f.getSeverityHint() != null && !f.getSeverityHint().isBlank())
                .filter(f -> f.getAttempts().stream()
                        .noneMatch(a -> FindingAttempt.OUTCOME_IN_PROGRESS.equals(a.getOutcome())))
                .map(DashboardFindingRowResponse::new)
                .collect(Collectors.toList());

        List<StrategyRankingRowResponse> ranking = calcularRanking(findings);

        List<Finding> resolvidos = findings.stream()
                .filter(f -> Finding.STATUS_RESOLVED.equals(f.getStatus()))
                .filter(f -> f.getOpenedAt() != null && f.getClosedAt() != null)
                .collect(Collectors.toList());
        Double avgResolutionSeconds = resolvidos.isEmpty() ? null : resolvidos.stream()
                .mapToLong(f -> Duration.between(f.getOpenedAt(), f.getClosedAt()).getSeconds())
                .average()
                .orElse(0);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (String status : List.of(Finding.STATUS_AWAITING_STRATEGY, Finding.STATUS_IN_PROGRESS,
                Finding.STATUS_RESOLVED, Finding.STATUS_DISMISSED, Finding.STATUS_RECURRED)) {
            statusCounts.put(status, findings.stream().filter(f -> status.equals(f.getStatus())).count());
        }

        long totalAttempts = findings.stream().mapToLong(f -> f.getAttempts().size()).sum();
        long open = statusCounts.get(Finding.STATUS_AWAITING_STRATEGY) + statusCounts.get(Finding.STATUS_IN_PROGRESS);
        Map<String, Long> totals = Map.of(
                "findings", (long) findings.size(),
                "open", open,
                "attempts", totalAttempts
        );

        List<OpenSuggestionResponse> suggestionsResponse = null;
        if (openSuggestions != null) {
            suggestionsResponse = openSuggestions.stream()
                    .map(s -> new OpenSuggestionResponse(s, findingsPorId.get(s.getFindingId())))
                    .collect(Collectors.toList());
        }

        return new DashboardPayloadResponse(scope, clientId, openQueue, ranking, severeSemTentativaAtiva,
                avgResolutionSeconds, statusCounts, totals, suggestionsResponse);
    }

    private List<StrategyRankingRowResponse> calcularRanking(List<Finding> findings) {
        Map<String, List<FindingAttempt>> porEstrategia = new LinkedHashMap<>();
        for (Finding finding : findings) {
            for (FindingAttempt attempt : finding.getAttempts()) {
                porEstrategia.computeIfAbsent(attempt.getStrategyKey(), k -> new ArrayList<>()).add(attempt);
            }
        }

        List<StrategyRankingRowResponse> resultado = new ArrayList<>();
        for (Map.Entry<String, List<FindingAttempt>> entry : porEstrategia.entrySet()) {
            List<FindingAttempt> attempts = entry.getValue();
            long worked = contarPorOutcome(attempts, FindingAttempt.OUTCOME_WORKED);
            long failed = contarPorOutcome(attempts, FindingAttempt.OUTCOME_FAILED);
            long partial = contarPorOutcome(attempts, FindingAttempt.OUTCOME_PARTIAL);
            long inProgress = contarPorOutcome(attempts, FindingAttempt.OUTCOME_IN_PROGRESS);
            String label = attempts.get(0).getStrategyLabel() != null ? attempts.get(0).getStrategyLabel() : entry.getKey();
            resultado.add(new StrategyRankingRowResponse(entry.getKey(), label, worked, failed, partial, inProgress));
        }
        return resultado;
    }

    private long contarPorOutcome(List<FindingAttempt> attempts, String outcome) {
        return attempts.stream().filter(a -> outcome.equals(a.getOutcome())).count();
    }
}
