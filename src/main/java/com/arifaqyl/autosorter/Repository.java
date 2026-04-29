package com.arifaqyl.autosorter;

import java.util.List;

/**
 * Represents a single GitHub repository with its metadata.
 */
public class Repository {

    private final String name;
    private final String description;
    private final String language;
    private final List<String> currentTopics;
    private final boolean isFork;
    private final String fullName;

    public Repository(String name, String description, String language,
                      List<String> currentTopics, boolean isFork, String fullName) {
        this.name        = name;
        this.description = description != null ? description : "";
        this.language    = language    != null ? language    : "";
        this.currentTopics = currentTopics;
        this.isFork      = isFork;
        this.fullName    = fullName;
    }

    public String getName()              { return name; }
    public String getDescription()       { return description; }
    public String getLanguage()          { return language; }
    public List<String> getCurrentTopics() { return currentTopics; }
    public boolean isFork()              { return isFork; }
    public String getFullName()          { return fullName; }

    @Override
    public String toString() {
        return String.format("%-40s | %-12s | fork:%-5s | topics: %s",
                name, language, isFork, currentTopics);
    }
}
