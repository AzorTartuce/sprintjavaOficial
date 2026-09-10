package com.sprintjava.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprintjava.connection.ConnectionFactory;
import com.sprintjava.model.Strategy;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StrategyDAO {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Strategy inserir(Strategy strategy) throws SQLException {
        String sql = "INSERT INTO strategies (strategy_key, label, applies_to_json, is_builtin, custom) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, strategy.getKey());
            stmt.setString(2, strategy.getLabel());
            stmt.setString(3, escreverAppliesTo(strategy.getAppliesTo()));
            stmt.setInt(4, strategy.isBuiltin() ? 1 : 0);
            stmt.setInt(5, strategy.isCustom() ? 1 : 0);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    strategy.setId(keys.getLong(1));
                }
            }
        }
        return strategy;
    }

    public List<Strategy> listarTodas() throws SQLException {
        String sql = "SELECT * FROM strategies ORDER BY id ASC";
        List<Strategy> strategies = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                strategies.add(mapRow(rs));
            }
        }
        return strategies;
    }

    public Strategy buscarPorKey(String key) throws SQLException {
        String sql = "SELECT * FROM strategies WHERE strategy_key = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean atualizar(Strategy strategy) throws SQLException {
        String sql = "UPDATE strategies SET label = ?, applies_to_json = ?, custom = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, strategy.getLabel());
            stmt.setString(2, escreverAppliesTo(strategy.getAppliesTo()));
            stmt.setInt(3, strategy.isCustom() ? 1 : 0);
            stmt.setLong(4, strategy.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM strategies WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private String escreverAppliesTo(List<String> appliesTo) throws SQLException {
        try {
            return objectMapper.writeValueAsString(appliesTo != null ? appliesTo : List.of());
        } catch (Exception e) {
            throw new SQLException("Erro ao serializar applies_to da estratégia", e);
        }
    }

    private Strategy mapRow(ResultSet rs) throws SQLException {
        List<String> appliesTo;
        try {
            appliesTo = objectMapper.readValue(rs.getString("applies_to_json"), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            appliesTo = List.of();
        }
        return new Strategy(
                rs.getLong("id"),
                rs.getString("strategy_key"),
                rs.getString("label"),
                appliesTo,
                rs.getInt("is_builtin") == 1,
                rs.getInt("custom") == 1
        );
    }
}
