package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.Suggestion;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SuggestionDAO {

    public Suggestion inserir(Suggestion suggestion) throws SQLException {
        String sql = "INSERT INTO suggestions (finding_id, triggered_by_meeting_id, reason, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, suggestion.getFindingId());
            setNullableLong(stmt, 2, suggestion.getTriggeredByMeetingId());
            stmt.setString(3, suggestion.getReason());
            stmt.setString(4, suggestion.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(suggestion.getCreatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    suggestion.setId(keys.getLong(1));
                }
            }
        }
        return suggestion;
    }

    public Suggestion buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM suggestions WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Suggestion> listarAbertasPorUsuario(Long userId) throws SQLException {
        String sql = "SELECT s.* FROM suggestions s "
                + "JOIN findings f ON f.id = s.finding_id "
                + "JOIN clients c ON c.id = f.client_id "
                + "WHERE c.user_id = ? AND s.status = 'open' ORDER BY s.created_at DESC";
        List<Suggestion> suggestions = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(mapRow(rs));
                }
            }
        }
        return suggestions;
    }

    public List<Suggestion> listarAbertasPorCliente(Long clientId) throws SQLException {
        String sql = "SELECT s.* FROM suggestions s "
                + "JOIN findings f ON f.id = s.finding_id "
                + "WHERE f.client_id = ? AND s.status = 'open' ORDER BY s.created_at DESC";
        List<Suggestion> suggestions = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(mapRow(rs));
                }
            }
        }
        return suggestions;
    }

    public boolean atualizar(Suggestion suggestion) throws SQLException {
        String sql = "UPDATE suggestions SET status = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, suggestion.getStatus());
            stmt.setLong(2, suggestion.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM suggestions WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private void setNullableLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.NUMERIC);
        }
    }

    private Suggestion mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        long triggeredBy = rs.getLong("triggered_by_meeting_id");
        Long triggeredByOrNull = rs.wasNull() ? null : triggeredBy;

        return new Suggestion(
                rs.getLong("id"),
                rs.getLong("finding_id"),
                triggeredByOrNull,
                rs.getString("reason"),
                rs.getString("status"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}
