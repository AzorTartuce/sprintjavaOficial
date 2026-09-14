package com.sprintjava.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class ConnectionFactory {

    private static volatile String url = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    private static volatile String user;
    private static volatile String password;

    @Component
    static class CredentialsInitializer {
        CredentialsInitializer(
                @Value("${ORACLE_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl}") String url,
                @Value("${ORACLE_USER:}") String user,
                @Value("${ORACLE_PASSWORD:}") String password) {
            ConnectionFactory.url = url;
            ConnectionFactory.user = user;
            ConnectionFactory.password = password;
        }
    }

    public static Connection getConnection() throws SQLException {
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
