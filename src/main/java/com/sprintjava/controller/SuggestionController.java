package com.sprintjava.controller;

import com.sprintjava.controller.dto.SuggestionDecisionRequest;
import com.sprintjava.controller.dto.SuggestionDecisionResponse;
import com.sprintjava.dao.ClientDAO;
import com.sprintjava.dao.FindingAttemptDAO;
import com.sprintjava.dao.FindingDAO;
import com.sprintjava.dao.SuggestionDAO;
import com.sprintjava.exception.ApiException;
import com.sprintjava.model.Client;
import com.sprintjava.model.Finding;
import com.sprintjava.model.Suggestion;
import com.sprintjava.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionDAO suggestionDAO;
    private final FindingDAO findingDAO;
    private final FindingAttemptDAO findingAttemptDAO;
    private final ClientDAO clientDAO;

    public SuggestionController(SuggestionDAO suggestionDAO, FindingDAO findingDAO,
                                 FindingAttemptDAO findingAttemptDAO, ClientDAO clientDAO) {
        this.suggestionDAO = suggestionDAO;
        this.findingDAO = findingDAO;
        this.findingAttemptDAO = findingAttemptDAO;
        this.clientDAO = clientDAO;
    }

    @PostMapping("/{id}")
    public SuggestionDecisionResponse decidir(Authentication authentication, @PathVariable Long id,
                                               @RequestBody SuggestionDecisionRequest request) {
        try {
            Suggestion suggestion = suggestionDAO.buscarPorId(id);
            if (suggestion == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Sugestão não encontrada");
            }
            Finding finding = findingDAO.buscarPorId(suggestion.getFindingId());
            if (finding == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Finding da sugestão não encontrado");
            }
            Client client = clientDAO.buscarPorId(finding.getClientId());
            Long userId = ((User) authentication.getPrincipal()).getId();
            if (client == null || !client.getUserId().equals(userId)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Sugestão não encontrada");
            }

            String decisao = request.getDecision();
            suggestion.aplicarDecisao(decisao);
            suggestionDAO.atualizar(suggestion);

            if (Suggestion.DECISION_CONFIRM_RESOLVED.equals(decisao)) {
                finding.atualizarStatus(Finding.STATUS_RESOLVED);
            } else if (Suggestion.DECISION_RECURRED.equals(decisao)) {
                finding.atualizarStatus(Finding.STATUS_RECURRED);
            } else {
                finding.atualizarStatus(Finding.STATUS_IN_PROGRESS);
            }
            findingDAO.atualizar(finding);
            finding.setAttempts(findingAttemptDAO.listarPorFinding(finding.getId()));

            return new SuggestionDecisionResponse(suggestion, finding);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao decidir sugestão", e);
        }
    }
}
