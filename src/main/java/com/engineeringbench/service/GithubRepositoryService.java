package com.engineeringbench.service;

import com.engineeringbench.config.GithubProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GithubRepositoryService {

    private final GithubProperties githubProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GithubRepositoryService(
            GithubProperties githubProperties,
            ObjectMapper objectMapper) {

        this.githubProperties = githubProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public GithubRepository createRepository(
            String name,
            String description) throws Exception {

        String requestBody = objectMapper.writeValueAsString(
                new CreateRepositoryRequest(
                        name,
                        description,
                        false
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.github.com/user/repos"))
                .header(
                        "Authorization",
                        "Bearer " + githubProperties.getGithubToken())
                .header(
                        "Accept",
                        "application/vnd.github+json")
                .header(
                        "X-GitHub-Api-Version",
                        "2022-11-28")
                .header(
                        "Content-Type",
                        "application/json")
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IOException(
                    "GitHub repository creation failed. "
                            + "HTTP status: "
                            + response.statusCode()
                            + ", response: "
                            + response.body());
        }

        JsonNode json =
                objectMapper.readTree(response.body());

        return new GithubRepository(
                json.get("name").asText(),
                json.get("full_name").asText(),
                json.get("html_url").asText(),
                json.get("clone_url").asText(),
                json.get("private").asBoolean()
        );
    }

    public GithubRepository findRepository(String name) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.github.com/repos/"
                                + getAuthenticatedUsername()
                                + "/"
                                + name))
                .header(
                        "Authorization",
                        "Bearer " + githubProperties.getGithubToken())
                .header(
                        "Accept",
                        "application/vnd.github+json")
                .header(
                        "X-GitHub-Api-Version",
                        "2022-11-28")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return null;
        }

        if (response.statusCode() != 200) {
            throw new IOException(
                    "GitHub repository lookup failed. "
                            + "HTTP status: "
                            + response.statusCode()
                            + ", response: "
                            + response.body());
        }

        JsonNode json =
                objectMapper.readTree(response.body());

        return new GithubRepository(
                json.get("name").asText(),
                json.get("full_name").asText(),
                json.get("html_url").asText(),
                json.get("clone_url").asText(),
                json.get("private").asBoolean()
        );
    }

    private record CreateRepositoryRequest(
            String name,
            String description,
            @com.fasterxml.jackson.annotation.JsonProperty("private")
            boolean privateRepository) {
    }

    public record GithubRepository(
            String name,
            String fullName,
            String htmlUrl,
            String cloneUrl,
            boolean privateRepository) {
    }

    private String getAuthenticatedUsername() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/user"))
                .header(
                        "Authorization",
                        "Bearer " + githubProperties.getGithubToken())
                .header(
                        "Accept",
                        "application/vnd.github+json")
                .header(
                        "X-GitHub-Api-Version",
                        "2022-11-28")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "GitHub user lookup failed. "
                            + "HTTP status: "
                            + response.statusCode()
                            + ", response: "
                            + response.body());
        }

        JsonNode json =
                objectMapper.readTree(response.body());

        return json.get("login").asText();
    }
}