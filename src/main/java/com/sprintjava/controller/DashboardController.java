package com.sprintjava.controller;

import com.sprintjava.controller.dto.DashboardPayloadResponse;
import com.sprintjava.dao.ClientDAO;
import com.sprintjava.dao.FindingAttemptDAO;
import com.sprintjava.dao.FindingDAO;
import com.sprintjava.dao.SuggestionDAO;
import com.sprintjava.exception.ApiException;
import com.sprintjava.model.Client;
import com.sprintjava.model.Finding;
import com.sprintjava.model.Suggestion;
import com.sprintjava.model.User;
import com.sprintjava.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class DashboardController {

    private final FindingDAO findingDAO;
    private final FindingAttemptDAO findingAttemptDAO;
    private final SuggestionDAO suggestionDAO;
    private final ClientDAO clientDAO;
    private final DashboardService dashboardService;

    public DashboardController(FindingDAO findingDAO, FindingAttemptDAO findingAttemptDAO,
                                SuggestionDAO suggestionDAO, ClientDAO clientDAO, DashboardService dashboardService) {
        this.findingDAO = findingDAO;
        this.findingAttemptDAO = findingAttemptDAO;
        this.suggestionDAO = suggestionDAO;
        this.clientDAO = clientDAO;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard")
    public DashboardPayloadResponse dashboardGeral(Authentication authentication) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        try {
            List<Finding> findings = carregarComTentativas(findingDAO.listarPorUsuario(userId));
            List<Suggestion> abertas = suggestionDAO.listarAbertasPorUsuario(userId);
            return dashboardService.montar("geral", null, findings, abertas, indexarPorId(findings));
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao montar dashboard", e);
        }
    }

    @GetMapping("/api/clients/{id}/dashboard")
    public DashboardPayloadResponse dashboardDoCliente(Authentication authentication, @PathVariable Long id) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        try {
            Client client = clientDAO.buscarPorId(id);
            if (client == null || !client.getUserId().equals(userId)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
            }
            List<Finding> findings = carregarComTentativas(findingDAO.listarPorCliente(id));
            List<Suggestion> abertas = suggestionDAO.listarAbertasPorCliente(id);
            return dashboardService.montar("client", id, findings, abertas, indexarPorId(findings));
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao montar dashboard do cliente", e);
        }
    }

    private List<Finding> carregarComTentativas(List<Finding> findings) throws SQLException {
        for (Finding finding : findings) {
            finding.setAttempts(findingAttemptDAO.listarPorFinding(finding.getId()));
        }
        return findings;
    }

    private Map<Long, Finding> indexarPorId(List<Finding> findings) {
        return findings.stream().collect(Collectors.toMap(Finding::getId, Function.identity()));
    }
}
