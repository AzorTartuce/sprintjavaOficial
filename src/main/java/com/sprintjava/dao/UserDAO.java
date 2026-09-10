package com.sprintjava.dao;

import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.User;
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
public class UserDAO {

    public User inserir(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, full_name, company, job_title, department, "
                + "phone, city, state, linkedin_url, bio, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getCompany());
            stmt.setString(5, user.getJobTitle());
            stmt.setString(6, user.getDepartment());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getCity());
            stmt.setString(9, user.getState());
            stmt.setString(10, user.getLinkedinUrl());
            stmt.setString(11, user.getBio());
            stmt.setTimestamp(12, Timestamp.valueOf(user.getCreatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }
        return user;
    }

    public User buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<User> listarTodos() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    public boolean atualizar(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, password_hash = ?, full_name = ?, company = ?, "
                + "job_title = ?, department = ?, phone = ?, city = ?, state = ?, linkedin_url = ?, bio = ? "
                + "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getCompany());
            stmt.setString(5, user.getJobTitle());
            stmt.setString(6, user.getDepartment());
            stmt.setString(7, user.getPhone());
            stmt.setString(8, user.getCity());
            stmt.setString(9, user.getState());
            stmt.setString(10, user.getLinkedinUrl());
            stmt.setString(11, user.getBio());
            stmt.setLong(12, user.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getString("company"),
                rs.getString("job_title"),
                rs.getString("department"),
                rs.getString("phone"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("linkedin_url"),
                rs.getString("bio"),
                createdAt != null ? createdAt.toLocalDateTime() : null
        );
    }
}
