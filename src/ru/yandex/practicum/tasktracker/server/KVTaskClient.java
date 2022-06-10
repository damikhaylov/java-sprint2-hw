package ru.yandex.practicum.tasktracker.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class KVTaskClient {
    private final String serverUrl;
    private final HttpClient client;
    private final String apiToken;

    public KVTaskClient(String serverUrl) {
        this.serverUrl = serverUrl;
        client = HttpClient.newHttpClient();
        apiToken = getBodyContent("/register");
    }

    public void put(String key, String json) {
        URI uri = URI.create(serverUrl + "/save/" + key + "?API_TOKEN=" + apiToken);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
            Optional<HttpResponse<String>> response = sendRequest(request);
            if (response.isPresent() && response.get().statusCode() != 200) {
                throw new RuntimeException("KV-сервер отклонил запрос с кодом " + response.get().statusCode());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Адрес KV-сервера не соответствует формату URL.");
        }
    }

    public String load(String key) {
        return getBodyContent("/load/" + key + "?API_TOKEN=" + apiToken);
    }

    private String getBodyContent(String endpoint) {
        URI uri = URI.create(serverUrl + endpoint);
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            Optional<HttpResponse<String>> response = sendRequest(request);
            if (response.isPresent() && response.get().statusCode() != 200) {
                throw new RuntimeException("KV-сервер отклонил запрос с кодом " + response.get().statusCode());
            }
            return (response.isPresent()) ? response.get().body() : "";
        } catch (IllegalArgumentException e) {
            System.out.println("Адрес KV-сервера не соответствует формату URL.");
            return "";
        }
    }

    private Optional<HttpResponse<String>> sendRequest(HttpRequest request) {
        try {
            return Optional.of(client.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса к KV-серверу возникла ошибка.");
            return Optional.empty();
        }
    }
}