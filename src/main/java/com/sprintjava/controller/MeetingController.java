package com.sprintjava.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintjava.controller.dto.MeetingDetailResponse;
import com.sprintjava.controller.dto.MeetingReviewRequest;
import com.sprintjava.controller.dto.MeetingReviewResponse;
import com.sprintjava.controller.dto.ReviewItemRequest;
import com.sprintjava.controller.dto.ReviewItemResponse;
import com.sprintjava.dao.ClientDAO;
import com.sprintjava.dao.MeetingDAO;
import com.sprintjava.exception.ApiException;
import com.sprintjava.model.Client;
import com.sprintjava.model.Meeting;
import com.sprintjava.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingDAO meetingDAO;
    private final ClientDAO clientDAO;
    private final ObjectMapper objectMapper;

    public MeetingController(MeetingDAO meetingDAO, ClientDAO clientDAO, ObjectMapper objectMapper) {
        this.meetingDAO = meetingDAO;
        this.clientDAO = clientDAO;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}")
    public MeetingDetailResponse buscarPorId(Authentication authentication, @PathVariable Long id) {
        Meeting meeting = buscarReuniaoDoUsuario(authentication, id);
        Client client = buscarCliente(meeting);
        return new MeetingDetailResponse(meeting, client.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(Authentication authentication, @PathVariable Long id) {
        Meeting meeting = buscarReuniaoDoUsuario(authentication, id);
        try {
            meetingDAO.deletar(meeting.getId());
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao remover reunião", e);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/review")
    public MeetingReviewResponse verRevisao(Authentication authentication, @PathVariable Long id) {
        Meeting meeting = buscarReuniaoDoUsuario(authentication, id);
        MeetingReviewResponse review = carregarRevisao(meeting);
        if (review == null || review.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Ainda não existe revisão para essa reunião");
        }
        return review;
    }

    @PostMapping("/{id}/review")
    public MeetingReviewResponse enviarRevisao(Authentication authentication, @PathVariable Long id,
                                                @RequestBody MeetingReviewRequest request) {
        Meeting meeting = buscarReuniaoDoUsuario(authentication, id);
        MeetingReviewResponse review = carregarRevisao(meeting);
        if (review == null) {
            review = new MeetingReviewResponse(meeting.getId(), new ArrayList<>());
        }

        Map<String, ReviewItemResponse> porEixo = new LinkedHashMap<>();
        for (ReviewItemResponse item : review.getItems()) {
            porEixo.put(item.getAxis(), item);
        }

        List<ReviewItemRequest> decisoes = request.getDecisions() != null ? request.getDecisions() : List.of();
        for (ReviewItemRequest decisao : decisoes) {
            String status = traduzirDecisao(decisao.getDecision());
            porEixo.put(decisao.getAxis(), new ReviewItemResponse(decisao.getAxis(), status, null, LocalDateTime.now()));
        }

        review.setItems(new ArrayList<>(porEixo.values()));

        try {
            meeting.setReviewJson(objectMapper.writeValueAsString(review));
            meetingDAO.atualizar(meeting);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar revisão", e);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Corpo da revisão inválido");
        }
        return review;
    }

    private String traduzirDecisao(String decisao) {
        if (decisao == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Decisão de revisão é obrigatória");
        }
        return switch (decisao) {
            case "confirm" -> "confirmed";
            case "dismiss" -> "dismissed";
            case "pending" -> "pending";
            default -> throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Decisão de revisão inválida: " + decisao);
        };
    }

    private MeetingReviewResponse carregarRevisao(Meeting meeting) {
        try {
            return objectMapper.readValue(meeting.getReviewJson(), MeetingReviewResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Client buscarCliente(Meeting meeting) {
        try {
            return clientDAO.buscarPorId(meeting.getClientId());
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar cliente da reunião", e);
        }
    }

    private Meeting buscarReuniaoDoUsuario(Authentication authentication, Long id) {
        try {
            Meeting meeting = meetingDAO.buscarPorId(id);
            if (meeting == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Reunião não encontrada");
            }
            Client client = clientDAO.buscarPorId(meeting.getClientId());
            Long userId = ((User) authentication.getPrincipal()).getId();
            if (client == null || !client.getUserId().equals(userId)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Reunião não encontrada");
            }
            return meeting;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar reunião", e);
        }
    }
}
