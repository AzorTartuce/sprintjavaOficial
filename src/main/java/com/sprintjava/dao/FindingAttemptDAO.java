package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.FindingAttempt;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FindingAttemptDAO {

    public FindingAttempt inserir(FindingAttempt attempt) throws SQLException {
        String sql = "INSERT INTO finding_attempts (finding_id, strategy_key, strategy_label, notes, outcome, "
                + "started_at, outcome_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, attempt.getFindingId());
            stmt.setString(2, attempt.getStrategyKey());
            stmt.setString(3, attempt.getStrategyLabel());
            stmt.setString(4, attempt.getNotes());
            stmt.setString(5, attempt.getOutcome());
            setNullableTimestamp(stmt, 6, attempt.getStartedAt());
            setNullableTimestamp(stmt, 7, attempt.getOutcomeAt());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    attempt.setId(keys.getLong(1));
                }
            }
        }
        return attempt;
    }

    public FindingAttempt buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM finding_attempts WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<FindingAttempt> listarPorFinding(Long findingId) throws SQLException {
        String sql = "SELECT * FROM finding_attempts WHERE finding_id = ? ORDER BY started_at ASC";
        List<FindingAttempt> attempts = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, findingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapRow(rs));
                }
            }
        }
        return attempts;
    }

    public List<FindingAttempt> listarPorUsuario(Long userId) throws SQLException {
        String sql = "SELECT a.* FROM finding_attempts a "
                + "JOIN findings f ON f.id = a.finding_id "
                + "JOIN clients c ON c.id = f.client_id WHERE c.user_id = ?";
        List<FindingAttempt> attempts = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapRow(rs));
                }
            }
        }
        return attempts;
    }

    public boolean atualizar(FindingAttempt attempt) throws SQLException {
        String sql = "UPDATE finding_attempts SET outcome = ?, notes = ?, outcome_at = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attempt.getOutcome());
            stmt.setString(2, attempt.getNotes());
            setNullableTimestamp(stmt, 3, attempt.getOutcomeAt());
            stmt.setLong(4, attempt.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM finding_attempts WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private void setNullableTimestamp(PreparedStatement stmt, int index, LocalDateTime value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(index, Timestamp.valueOf(value));
        } else {
            stmt.setNull(index, Types.TIMESTAMP);
        }
    }

    private FindingAttempt mapRow(ResultSet rs) throws SQLException {
        Timestamp startedAt = rs.getTimestamp("started_at");
        Timestamp outcomeAt = rs.getTimestamp("outcome_at");

        return new FindingAttempt(
                rs.getLong("id"),
                rs.getLong("finding_id"),
                rs.getString("strategy_key"),
                rs.getString("strategy_label"),
                rs.getString("notes"),
                rs.getString("outcome"),
                startedAt != null ? startedAt.toLocalDateTime() : null,
                outcomeAt != null ? outcomeAt.toLocalDateTime() : null
        );
    }
}
