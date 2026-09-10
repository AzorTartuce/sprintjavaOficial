package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.Meeting;
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
public class MeetingDAO {

    public Meeting inserir(Meeting meeting) throws SQLException {
        String sql = "INSERT INTO meetings (client_id, source_filename, created_at, triage, "
                + "selected_agents_json, final_report_json, reports_json, review_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, meeting.getClientId());
            stmt.setString(2, meeting.getSourceFilename());
            stmt.setTimestamp(3, Timestamp.valueOf(meeting.getCreatedAt()));
            stmt.setString(4, meeting.getTriage());
            stmt.setString(5, meeting.getSelectedAgentsJson());
            stmt.setString(6, meeting.getFinalReportJson());
            stmt.setString(7, meeting.getReportsJson());
            stmt.setString(8, meeting.getReviewJson());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    meeting.setId(keys.getLong(1));
                }
            }
        }
        return meeting;
    }

    public Meeting buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM meetings WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Meeting> listarPorCliente(Long clientId) throws SQLException {
        String sql = "SELECT * FROM meetings WHERE client_id = ? ORDER BY created_at DESC";
        List<Meeting> meetings = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    meetings.add(mapRow(rs));
                }
            }
        }
        return meetings;
    }

    public List<Meeting> listarTodas() throws SQLException {
        String sql = "SELECT * FROM meetings ORDER BY created_at DESC";
        List<Meeting> meetings = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                meetings.add(mapRow(rs));
            }
        }
        return meetings;
    }

    public boolean atualizar(Meeting meeting) throws SQLException {
        String sql = "UPDATE meetings SET source_filename = ?, triage = ?, selected_agents_json = ?, "
                + "final_report_json = ?, reports_json = ?, review_json = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, meeting.getSourceFilename());
            stmt.setString(2, meeting.getTriage());
            stmt.setString(3, meeting.getSelectedAgentsJson());
            stmt.setString(4, meeting.getFinalReportJson());
            stmt.setString(5, meeting.getReportsJson());
            stmt.setString(6, meeting.getReviewJson());
            stmt.setLong(7, meeting.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM meetings WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Meeting mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Meeting(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("source_filename"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                rs.getString("triage"),
                rs.getString("selected_agents_json"),
                rs.getString("final_report_json"),
                rs.getString("reports_json"),
                rs.getString("review_json")
        );
    }
}
