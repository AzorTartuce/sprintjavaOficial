package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.AnalysisJob;
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
public class AnalysisJobDAO {

    public AnalysisJob inserir(AnalysisJob job) throws SQLException {
        String sql = "INSERT INTO analysis_jobs (user_id, client_id, source_filename, input_text, status, "
                + "error_detail, meeting_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, job.getUserId());
            stmt.setLong(2, job.getClientId());
            stmt.setString(3, job.getSourceFilename());
            stmt.setString(4, job.getInputText());
            stmt.setString(5, job.getStatus());
            stmt.setString(6, job.getErrorDetail());
            if (job.getMeetingId() != null) {
                stmt.setLong(7, job.getMeetingId());
            } else {
                stmt.setNull(7, java.sql.Types.NUMERIC);
            }
            stmt.setTimestamp(8, Timestamp.valueOf(job.getCreatedAt()));
            stmt.setTimestamp(9, Timestamp.valueOf(job.getUpdatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    job.setId(keys.getLong(1));
                }
            }
        }
        return job;
    }

    public AnalysisJob buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM analysis_jobs WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<AnalysisJob> listarPorStatus(String status) throws SQLException {
        String sql = "SELECT * FROM analysis_jobs WHERE status = ? ORDER BY created_at ASC";
        List<AnalysisJob> jobs = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapRow(rs));
                }
            }
        }
        return jobs;
    }

    public List<AnalysisJob> listarTodos() throws SQLException {
        String sql = "SELECT * FROM analysis_jobs ORDER BY created_at DESC";
        List<AnalysisJob> jobs = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                jobs.add(mapRow(rs));
            }
        }
        return jobs;
    }

    public boolean atualizar(AnalysisJob job) throws SQLException {
        String sql = "UPDATE analysis_jobs SET status = ?, error_detail = ?, meeting_id = ?, updated_at = ? "
                + "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, job.getStatus());
            stmt.setString(2, job.getErrorDetail());
            if (job.getMeetingId() != null) {
                stmt.setLong(3, job.getMeetingId());
            } else {
                stmt.setNull(3, java.sql.Types.NUMERIC);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(job.getUpdatedAt()));
            stmt.setLong(5, job.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM analysis_jobs WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private AnalysisJob mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        long meetingId = rs.getLong("meeting_id");
        Long meetingIdOrNull = rs.wasNull() ? null : meetingId;

        return new AnalysisJob(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("client_id"),
                rs.getString("source_filename"),
                rs.getString("input_text"),
                rs.getString("status"),
                rs.getString("error_detail"),
                meetingIdOrNull,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );
    }
}
