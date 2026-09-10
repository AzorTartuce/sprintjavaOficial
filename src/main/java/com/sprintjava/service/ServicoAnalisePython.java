package com.sprintjava.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;


@Service
public class ServicoAnalisePython {

    private static final String URL_PADRAO = "http://localhost:5000/analisar";
    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(5);
    private static final Duration TIMEOUT_RESPOSTA = Duration.ofMinutes(25);

    private final HttpClient httpClient;
    private final URI endpoint;

    public ServicoAnalisePython() {
        this(System.getenv().getOrDefault("PYTHON_ANALISE_URL", URL_PADRAO));
    }

    public ServicoAnalisePython(String url) {
        this.endpoint = URI.create(url);
        this.httpClient = HttpClient.newBuilder()
                
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT_CONEXAO)
                .build();
    }

    public String enviarParaProcessamento(byte[] bytesDoArquivo, String nomeDoArquivo) throws AnalisePythonException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(TIMEOUT_RESPOSTA)
                .header("Content-Type", "application/octet-stream")
                .header("X-File-Name", nomeDoArquivo)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytesDoArquivo))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new AnalisePythonException(
                    "Tempo limite excedido ao aguardar resposta do motor de análise (Python).", e);
        } catch (IOException e) {
            throw new AnalisePythonException(
                    "Motor de análise (Python) indisponível em " + endpoint, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnalisePythonException("Requisição ao motor de análise (Python) foi interrompida.", e);
        }

        if (response.statusCode() != 200) {
            throw new AnalisePythonException(
                    "Erro no motor de análise (Python). Status: " + response.statusCode()
                            + ", corpo: " + response.body());
        }

        return response.body();
    }
}
