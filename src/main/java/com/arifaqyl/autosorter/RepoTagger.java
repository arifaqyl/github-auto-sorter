package com.arifaqyl.autosorter;

import java.util.*;

/**
 * Analyses a repository's name, description, and language
 * and suggests relevant GitHub topics to apply.
 *
 * Tags are suggested based on keyword matching — no AI needed.
 * Existing topics are preserved and new ones are merged in.
 */
public class RepoTagger {

    // ─── Keyword → Topic mappings ─────────────────────────────────────────────

    private static final Map<String, List<String>> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        // Languages
        KEYWORD_MAP.put("java",       List.of("java"));
        KEYWORD_MAP.put("python",     List.of("python"));
        KEYWORD_MAP.put("javascript", List.of("javascript"));
        KEYWORD_MAP.put("typescript", List.of("typescript"));
        KEYWORD_MAP.put("kotlin",     List.of("kotlin"));
        KEYWORD_MAP.put("swift",      List.of("swift"));
        KEYWORD_MAP.put("c++",        List.of("cpp"));
        KEYWORD_MAP.put("cpp",        List.of("cpp"));
        KEYWORD_MAP.put("html",       List.of("html", "web"));
        KEYWORD_MAP.put("css",        List.of("css", "web"));

        // Project types
        KEYWORD_MAP.put("api",          List.of("api", "backend"));
        KEYWORD_MAP.put("rest",         List.of("rest-api", "backend"));
        KEYWORD_MAP.put("web",          List.of("web", "frontend"));
        KEYWORD_MAP.put("website",      List.of("web", "frontend"));
        KEYWORD_MAP.put("portfolio",    List.of("portfolio", "web"));
        KEYWORD_MAP.put("app",          List.of("application"));
        KEYWORD_MAP.put("cli",          List.of("cli", "command-line"));
        KEYWORD_MAP.put("tool",         List.of("tool", "utility"));
        KEYWORD_MAP.put("bot",          List.of("bot", "automation"));
        KEYWORD_MAP.put("automation",   List.of("automation"));
        KEYWORD_MAP.put("scraper",      List.of("web-scraping", "automation"));
        KEYWORD_MAP.put("crawler",      List.of("web-scraping", "automation"));
        KEYWORD_MAP.put("script",       List.of("scripting", "utility"));
        KEYWORD_MAP.put("game",         List.of("game", "game-dev"));
        KEYWORD_MAP.put("mobile",       List.of("mobile"));
        KEYWORD_MAP.put("android",      List.of("android", "mobile"));
        KEYWORD_MAP.put("ios",          List.of("ios", "mobile"));

        // CS concepts
        KEYWORD_MAP.put("sort",         List.of("algorithms", "data-structures"));
        KEYWORD_MAP.put("sorting",      List.of("algorithms", "data-structures"));
        KEYWORD_MAP.put("search",       List.of("algorithms"));
        KEYWORD_MAP.put("algorithm",    List.of("algorithms"));
        KEYWORD_MAP.put("data-struct",  List.of("data-structures"));
        KEYWORD_MAP.put("linked-list",  List.of("data-structures"));
        KEYWORD_MAP.put("tree",         List.of("data-structures", "algorithms"));
        KEYWORD_MAP.put("graph",        List.of("data-structures", "algorithms"));
        KEYWORD_MAP.put("database",     List.of("database"));
        KEYWORD_MAP.put("db",           List.of("database"));
        KEYWORD_MAP.put("sql",          List.of("sql", "database"));
        KEYWORD_MAP.put("mysql",        List.of("mysql", "database"));
        KEYWORD_MAP.put("mongodb",      List.of("mongodb", "database", "nosql"));

        // AI / ML
        KEYWORD_MAP.put("ai",           List.of("artificial-intelligence"));
        KEYWORD_MAP.put("ml",           List.of("machine-learning"));
        KEYWORD_MAP.put("machine-learn",List.of("machine-learning"));
        KEYWORD_MAP.put("neural",       List.of("deep-learning", "machine-learning"));
        KEYWORD_MAP.put("model",        List.of("machine-learning"));
        KEYWORD_MAP.put("chatbot",      List.of("chatbot", "artificial-intelligence"));
        KEYWORD_MAP.put("llm",          List.of("llm", "artificial-intelligence"));

        // University
        KEYWORD_MAP.put("assignment",   List.of("university", "coursework"));
        KEYWORD_MAP.put("homework",     List.of("university", "coursework"));
        KEYWORD_MAP.put("project",      List.of("project"));
        KEYWORD_MAP.put("lab",          List.of("university", "coursework"));
        KEYWORD_MAP.put("unikl",        List.of("university", "unikl"));

        // Tools / DevOps
        KEYWORD_MAP.put("docker",       List.of("docker", "devops"));
        KEYWORD_MAP.put("github",       List.of("github"));
        KEYWORD_MAP.put("git",          List.of("git"));
        KEYWORD_MAP.put("ci",           List.of("ci-cd", "devops"));
        KEYWORD_MAP.put("deploy",       List.of("deployment", "devops"));
    }

    // ─── Language → Topic mappings ────────────────────────────────────────────

    private static final Map<String, String> LANGUAGE_MAP = Map.of(
            "Java",       "java",
            "Python",     "python",
            "JavaScript", "javascript",
            "TypeScript", "typescript",
            "Kotlin",     "kotlin",
            "C++",        "cpp",
            "Swift",      "swift",
            "HTML",       "html",
            "CSS",        "css",
            "Go",         "golang"
    );

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Suggests topics for a repository.
     * Returns merged list of existing + new suggested topics (deduped, max 20).
     */
    public List<String> suggestTopics(Repository repo) {
        Set<String> topics = new LinkedHashSet<>(repo.getCurrentTopics());

        String searchText = (repo.getName() + " " + repo.getDescription()).toLowerCase();

        // Match keywords
        for (Map.Entry<String, List<String>> entry : KEYWORD_MAP.entrySet()) {
            if (searchText.contains(entry.getKey())) {
                topics.addAll(entry.getValue());
            }
        }

        // Add language tag
        String lang = LANGUAGE_MAP.get(repo.getLanguage());
        if (lang != null) topics.add(lang);

        // Add fork tag if applicable
        if (repo.isFork()) topics.add("fork");

        // GitHub max 20 topics per repo
        List<String> result = new ArrayList<>(topics);
        if (result.size() > 20) result = result.subList(0, 20);

        return result;
    }

    /**
     * Returns only the NEW topics that would be added (not already on the repo).
     */
    public List<String> getNewTopics(Repository repo) {
        List<String> suggested = suggestTopics(repo);
        List<String> existing  = repo.getCurrentTopics();
        List<String> newTopics = new ArrayList<>(suggested);
        newTopics.removeAll(existing);
        return newTopics;
    }
}
