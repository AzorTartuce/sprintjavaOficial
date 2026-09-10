package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.Finding;
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
public class FindingDAO {

    public Finding inserir(Finding finding) throws SQLException {
        String sql = "INSERT INTO findings (client_id, origin_meeting_id, finding_type, title, severity_hint, "
                + "status, opened_at, closed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, finding.getClientId());
            setNullableLong(stmt, 2, finding.getOriginMeetingId());
            stmt.setString(3, finding.getFindingType());
            stmt.setString(4, finding.getTitle());
            stmt.setString(5, finding.getSeverityHint());
            stmt.setString(6, finding.getStatus());
            setNullableTimestamp(stmt, 7, finding.getOpenedAt());
            setNullableTimestamp(stmt, 8, finding.getClosedAt());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    finding.setId(keys.getLong(1));
                }
            }
        }
        return finding;
    }

    public Finding buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM findings WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Finding> listarPorCliente(Long clientId) throws SQLException {
        String sql = "SELECT * FROM findings WHERE client_id = ? ORDER BY opened_at DESC";
        List<Finding> findings = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findings.add(mapRow(rs));
                }
            }
        }
        return findings;
    }

    public List<Finding> listarPorUsuario(Long userId) throws SQLException {
        String sql = "SELECT f.* FROM findings f JOIN clients c ON c.id = f.client_id WHERE c.user_id = ? "
                + "ORDER BY f.opened_at DESC";
        List<Finding> findings = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    findings.add(mapRow(rs));
                }
            }
        }
        return findings;
    }

    public boolean atualizar(Finding finding) throws SQLException {
        String sql = "UPDATE findings SET finding_type = ?, title = ?, severity_hint = ?, status = ?, "
                + "opened_at = ?, closed_at = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, finding.getFindingType());
            stmt.setString(2, finding.getTitle());
            stmt.setString(3, finding.getSeverityHint());
            stmt.setString(4, finding.getStatus());
            setNullableTimestamp(stmt, 5, finding.getOpenedAt());
            setNullableTimestamp(stmt, 6, finding.getClosedAt());
            stmt.setLong(7, finding.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM findings WHERE id = ?";
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

    private void setNullableTimestamp(PreparedStatement stmt, int index, java.time.LocalDateTime value)
            throws SQLException {
        if (value != null) {
            stmt.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            stmt.setNull(index, Types.TIMESTAMP);
        }
    }

    private Finding mapRow(ResultSet rs) throws SQLException {
        Timestamp openedAt = rs.getTimestamp("opened_at");
        Timestamp closedAt = rs.getTimestamp("closed_at");
        long originMeetingId = rs.getLong("origin_meeting_id");
        Long originMeetingIdOrNull = rs.wasNull() ? null : originMeetingId;

        return new Finding(
                rs.getLong("id"),
                rs.getLong("client_id"),
                originMeetingIdOrNull,
                rs.getString("finding_type"),
                rs.getString("title"),
                rs.getString("severity_hint"),
                rs.getString("status"),
                openedAt != null ? openedAt.toLocalDateTime() : null,
                closedAt != null ? closedAt.toLocalDateTime() : null
        );
    }
}
