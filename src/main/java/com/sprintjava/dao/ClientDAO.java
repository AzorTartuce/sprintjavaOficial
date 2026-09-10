package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.Client;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientDAO {

    public Client inserir(Client client) throws SQLException {
        String sql = "INSERT INTO clients (user_id, name, name_key, segment, company_size, website, city, "
                + "state, contact_name, contact_role, contact_email, contact_phone, owner, status, notes, "
                + "created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, client.getUserId());
            stmt.setString(2, client.getName());
            stmt.setString(3, client.getNameKey());
            stmt.setString(4, client.getSegment());
            stmt.setString(5, client.getCompanySize());
            stmt.setString(6, client.getWebsite());
            stmt.setString(7, client.getCity());
            stmt.setString(8, client.getState());
            stmt.setString(9, client.getContactName());
            stmt.setString(10, client.getContactRole());
            stmt.setString(11, client.getContactEmail());
            stmt.setString(12, client.getContactPhone());
            stmt.setString(13, client.getOwner());
            stmt.setString(14, client.getStatus());
            stmt.setString(15, client.getNotes());
            stmt.setTimestamp(16, Timestamp.valueOf(client.getCreatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getLong(1));
                }
            }
        }
        return client;
    }

    public Client buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Client> listarPorUsuario(Long userId) throws SQLException {
        String sql = "SELECT * FROM clients WHERE user_id = ? ORDER BY created_at DESC";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapRow(rs));
                }
            }
        }
        return clients;
    }

    public List<Client> listarTodos() throws SQLException {
        String sql = "SELECT * FROM clients ORDER BY created_at DESC";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }
        }
        return clients;
    }

    public boolean atualizar(Client client) throws SQLException {
        String sql = "UPDATE clients SET name = ?, name_key = ?, segment = ?, company_size = ?, website = ?, "
                + "city = ?, state = ?, contact_name = ?, contact_role = ?, contact_email = ?, contact_phone = ?, "
                + "owner = ?, status = ?, notes = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getName());
            stmt.setString(2, client.getNameKey());
            stmt.setString(3, client.getSegment());
            stmt.setString(4, client.getCompanySize());
            stmt.setString(5, client.getWebsite());
            stmt.setString(6, client.getCity());
            stmt.setString(7, client.getState());
            stmt.setString(8, client.getContactName());
            stmt.setString(9, client.getContactRole());
            stmt.setString(10, client.getContactEmail());
            stmt.setString(11, client.getContactPhone());
            stmt.setString(12, client.getOwner());
            stmt.setString(13, client.getStatus());
            stmt.setString(14, client.getNotes());
            stmt.setLong(15, client.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Client(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("name_key"),
                rs.getString("segment"),
                rs.getString("company_size"),
                rs.getString("website"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("contact_name"),
                rs.getString("contact_role"),
                rs.getString("contact_email"),
                rs.getString("contact_phone"),
                rs.getString("owner"),
                rs.getString("status"),
                rs.getString("notes"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}
