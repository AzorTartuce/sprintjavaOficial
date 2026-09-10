package com.sprintjava.controller;

import com.sprintjava.controller.dto.ClientDetailResponse;
import com.sprintjava.controller.dto.ClientListItemResponse;
import com.sprintjava.controller.dto.ClientRequest;
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
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientDAO clientDAO;
    private final MeetingDAO meetingDAO;

    public ClientController(ClientDAO clientDAO, MeetingDAO meetingDAO) {
        this.clientDAO = clientDAO;
        this.meetingDAO = meetingDAO;
    }

    @GetMapping
    public List<ClientListItemResponse> listarTodos(Authentication authentication) {
        try {
            List<Client> clients = clientDAO.listarPorUsuario(usuarioId(authentication));
            List<ClientListItemResponse> resultado = new java.util.ArrayList<>();
            for (Client client : clients) {
                resultado.add(paraListItem(client));
            }
            return resultado;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar clientes", e);
        }
    }

    @GetMapping("/{id}")
    public ClientDetailResponse buscarPorId(Authentication authentication, @PathVariable Long id) {
        Client client = buscarClienteDoUsuario(authentication, id);
        try {
            List<Meeting> meetings = meetingDAO.listarPorCliente(client.getId());
            return new ClientDetailResponse(client, meetings);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar cliente", e);
        }
    }

    @PostMapping
    public ResponseEntity<ClientListItemResponse> criar(Authentication authentication, @RequestBody ClientRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Nome do cliente é obrigatório");
        }
        Client client = new Client(usuarioId(authentication), request.getName());
        aplicarCampos(client, request);
        try {
            clientDAO.inserir(client);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar cliente", e);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClientListItemResponse(client, 0, null));
    }

    @PatchMapping("/{id}")
    public ClientDetailResponse atualizar(Authentication authentication, @PathVariable Long id, @RequestBody ClientRequest request) {
        Client client = buscarClienteDoUsuario(authentication, id);
        aplicarCampos(client, request);
        try {
            clientDAO.atualizar(client);
            List<Meeting> meetings = meetingDAO.listarPorCliente(client.getId());
            return new ClientDetailResponse(client, meetings);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar cliente", e);
        }
    }

    private ClientListItemResponse paraListItem(Client client) throws SQLException {
        List<Meeting> meetings = meetingDAO.listarPorCliente(client.getId());
        LocalDateTime ultima = meetings.stream()
                .map(Meeting::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new ClientListItemResponse(client, meetings.size(), ultima);
    }

    Client buscarClienteDoUsuario(Authentication authentication, Long id) {
        try {
            Client client = clientDAO.buscarPorId(id);
            if (client == null || !client.getUserId().equals(usuarioId(authentication))) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
            }
            return client;
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar cliente", e);
        }
    }

    private void aplicarCampos(Client client, ClientRequest request) {
        if (request.getName() != null) {
            client.setName(request.getName());
            client.setNameKey(client.gerarNameKey());
        }
        if (request.getSegment() != null) client.setSegment(request.getSegment());
        if (request.getCompanySize() != null) client.setCompanySize(request.getCompanySize());
        if (request.getWebsite() != null) client.setWebsite(request.getWebsite());
        if (request.getCity() != null) client.setCity(request.getCity());
        if (request.getState() != null) client.setState(request.getState());
        if (request.getContactName() != null) client.setContactName(request.getContactName());
        if (request.getContactRole() != null) client.setContactRole(request.getContactRole());
        if (request.getContactEmail() != null) client.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) client.setContactPhone(request.getContactPhone());
        if (request.getOwner() != null) client.setOwner(request.getOwner());
        if (request.getNotes() != null) client.setNotes(request.getNotes());
        if (request.getStatus() != null) client.atualizarStatus(request.getStatus());
    }

    private Long usuarioId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
