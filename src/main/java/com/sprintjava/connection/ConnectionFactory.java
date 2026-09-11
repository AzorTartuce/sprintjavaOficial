package com.sprintjava.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        String url = env("ORACLE_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl");
        String user = env("ORACLE_USER", null);
        String password = env("ORACLE_PASSWORD", null);
        return DriverManager.getConnection(url, user, password);
    }

    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao fechar a conexão com o banco de dados.", e);
        }
    }
}
