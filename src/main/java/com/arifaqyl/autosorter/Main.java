package com.arifaqyl.autosorter;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * GitHub Auto-Sorter
 *
 * Reads all public repositories for a GitHub user,
 * suggests relevant topics based on repo names/descriptions/language,
 * and optionally applies them via the GitHub API.
 *
 * Usage:
 *   java -jar github-auto-sorter.jar
 *
 * To apply changes you need a GitHub Personal Access Token (PAT):
 *   Settings → Developer settings → Personal access tokens → Fine-grained
 *   Permission: Repository → Topics (read & write)
 */
public class Main {

    // ANSI colours for cleaner terminal output
    private static final String RESET  = "\u001B[0m";
    private static final String BLUE   = "\u001B[34m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BOLD   = "\u001B[1m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printBanner();

        // ── Step 1: Get username ──────────────────────────────────────────────
        System.out.print(BLUE + "Enter GitHub username: " + RESET);
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("No username entered. Exiting.");
            return;
        }

        // ── Step 2: Optional token for writing ───────────────────────────────
        System.out.println();
        System.out.println(YELLOW + "To apply topic changes you need a GitHub Personal Access Token.");
        System.out.println("Leave blank to run in preview-only mode (no changes made)." + RESET);
        System.out.print(BLUE + "GitHub PAT (optional): " + RESET);
        String token = scanner.nextLine().trim();

        GitHubClient client = new GitHubClient(token.isEmpty() ? null : token);
        RepoTagger   tagger = new RepoTagger();

        // ── Step 3: Validate user & fetch repos ───────────────────────────────
        System.out.println();
        System.out.println(CYAN + "Fetching repositories for @" + username + "..." + RESET);

        List<Repository> repos;
        try {
            if (!client.userExists(username)) {
                System.out.println("User @" + username + " not found on GitHub.");
                return;
            }
            repos = client.getRepositories(username);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error connecting to GitHub API: " + e.getMessage());
            return;
        }

        if (repos.isEmpty()) {
            System.out.println("No public repositories found for @" + username);
            return;
        }

        System.out.printf("Found %d repositories.%n%n", repos.size());

        // ── Step 4: Analyse and display suggestions ───────────────────────────
        int reposWithSuggestions = 0;

        for (Repository repo : repos) {
            List<String> newTopics = tagger.getNewTopics(repo);

            if (newTopics.isEmpty()) {
                System.out.printf("  %s%-40s%s → already well tagged%n",
                        YELLOW, repo.getName(), RESET);
                continue;
            }

            reposWithSuggestions++;
            System.out.printf("  %s%-40s%s%n", BOLD, repo.getName(), RESET);
            System.out.printf("    Current : %s%n", repo.getCurrentTopics());
            System.out.printf("    Adding  : %s%s%s%n%n", GREEN, newTopics, RESET);
        }

        if (reposWithSuggestions == 0) {
            System.out.println(GREEN + "\nAll repositories are already well tagged. Nothing to do!" + RESET);
            return;
        }

        // ── Step 5: Apply if token provided ──────────────────────────────────
        if (token.isEmpty()) {
            System.out.println(YELLOW + "\nPreview mode — no changes made." + RESET);
            System.out.println("Run again with a GitHub PAT to apply these tags.");
            return;
        }

        System.out.print(BLUE + "\nApply these tags to @" + username + "'s repos? (yes/no): " + RESET);
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Cancelled — no changes made.");
            return;
        }

        System.out.println();
        int updated = 0;
        int failed  = 0;

        for (Repository repo : repos) {
            List<String> suggested = tagger.suggestTopics(repo);
            List<String> newTopics = tagger.getNewTopics(repo);

            if (newTopics.isEmpty()) continue;

            System.out.printf("  Updating %-40s ", repo.getName() + "...");

            try {
                boolean success = client.updateTopics(repo.getFullName(), suggested);
                if (success) {
                    System.out.println(GREEN + "done" + RESET);
                    updated++;
                } else {
                    System.out.println(YELLOW + "skipped" + RESET);
                    failed++;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println(YELLOW + "failed: " + e.getMessage() + RESET);
                failed++;
            }
        }

        // ── Step 6: Summary ───────────────────────────────────────────────────
        System.out.println();
        System.out.println("─────────────────────────────────────");
        System.out.printf(GREEN + "Done! %d repos updated" + RESET + ", %d skipped.%n", updated, failed);
        System.out.println("Visit https://github.com/" + username + " to see the results.");
    }

    private static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      GitHub Auto-Sorter v1.0          ║");
        System.out.println("║   by @arifaqyl  |  arifaqyl.me        ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println(RESET);
        System.out.println("Auto-tags GitHub repositories based on name, description & language.");
        System.out.println();
    }
}
