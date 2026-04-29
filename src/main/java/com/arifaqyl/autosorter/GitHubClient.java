package com.arifaqyl.autosorter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all communication with the GitHub REST API.
 *
 * Reading public repos requires no token.
 * Writing topics requires a Personal Access Token with 'repo' scope.
 */
public class GitHubClient {

    private static final String BASE_URL  = "https://api.github.com";
    private static final String USER_AGENT = "github-auto-sorter/1.0";

    private final HttpClient http;
    private final String token; // null = read-only mode

    public GitHubClient(String token) {
        this.http  = HttpClient.newHttpClient();
        this.token = token;
    }

    // ─── Fetch all repos for a user ──────────────────────────────────────────

    public List<Repository> getRepositories(String username) throws IOException, InterruptedException {
        List<Repository> repos = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = String.format("%s/users/%s/repos?per_page=100&page=%d", BASE_URL, username, page);
            String body = get(url);
            JSONArray arr = new JSONArray(body);

            if (arr.isEmpty()) break;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                repos.add(parseRepo(obj));
            }
            page++;
        }

        return repos;
    }

    // ─── Update topics on a repo ─────────────────────────────────────────────

    public boolean updateTopics(String repoFullName, List<String> topics) throws IOException, InterruptedException {
        if (token == null || token.isBlank()) {
            System.out.println("  [!] No token provided — skipping write for: " + repoFullName);
            return false;
        }

        String url  = String.format("%s/repos/%s/topics", BASE_URL, repoFullName);
        JSONArray arr = new JSONArray(topics);
        JSONObject body = new JSONObject();
        body.put("names", arr);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept",        "application/vnd.github.mercy-preview+json")
                .header("Content-Type",  "application/json")
                .header("User-Agent",    USER_AGENT)
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        
        if (res.statusCode() == 200) {
            return true;
        } else {
            System.out.println("\n  [!] GitHub API Error (" + res.statusCode() + "): " + res.body());
            return false;
        }
    }

    // ─── Validate a GitHub username ───────────────────────────────────────────

    public boolean userExists(String username) throws IOException, InterruptedException {
        String url = BASE_URL + "/users/" + username;
        HttpRequest req = buildGet(url);
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return res.statusCode() == 200;
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest req = buildGet(url);
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 404) throw new IOException("Not found: " + url);
        if (res.statusCode() != 200) throw new IOException("API error " + res.statusCode() + " for " + url);

        return res.body();
    }

    private HttpRequest buildGet(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept",     "application/vnd.github.mercy-preview+json")
                .header("User-Agent", USER_AGENT)
                .GET();

        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder.build();
    }

    private Repository parseRepo(JSONObject obj) {
        String name        = obj.optString("name", "");
        String description = obj.isNull("description") ? "" : obj.optString("description", "");
        String language    = obj.isNull("language")    ? "" : obj.optString("language",    "");
        boolean isFork     = obj.optBoolean("fork", false);
        String fullName    = obj.optString("full_name", "");

        List<String> topics = new ArrayList<>();
        if (obj.has("topics")) {
            JSONArray topicArr = obj.getJSONArray("topics");
            for (int i = 0; i < topicArr.length(); i++) {
                topics.add(topicArr.getString(i));
            }
        }

        return new Repository(name, description, language, topics, isFork, fullName);
    }
}
