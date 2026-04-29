# GitHub Auto-Sorter

Automatically tags and organises GitHub repositories using the GitHub REST API.

Built with Java 11 — no frameworks, no fluff.

## What it does

- Fetches all public repositories for any GitHub username
- Analyses each repo's name, description, and primary language
- Suggests relevant topics (e.g. `java`, `algorithms`, `web`, `automation`)
- Optionally applies the tags directly via the GitHub API

## Usage

**Preview mode** (no token needed — just shows suggestions):
```bash
java -jar github-auto-sorter.jar
# Enter username, leave token blank
```

**Apply mode** (requires a GitHub Personal Access Token):
```bash
java -jar github-auto-sorter.jar
# Enter username + PAT with repo/topics write permission
```

## Building

```bash
mvn clean package
java -jar target/github-auto-sorter.jar
```

## Getting a Personal Access Token

1. GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens
2. Create token with **Repository** → **Topics** (Read and Write)
3. Paste when prompted

## Project structure

```
src/
└── main/java/com/arifaqyl/autosorter/
    ├── Main.java          # Entry point + CLI
    ├── GitHubClient.java  # GitHub API communication
    ├── RepoTagger.java    # Tagging logic
    └── Repository.java    # Repo data model
```

## Tech

- Java 11 (`java.net.http.HttpClient` — zero external HTTP deps)
- `org.json` for JSON parsing
- GitHub REST API v3

---

Built by [Arif Aqyl](https://arifaqyl.me) · [@arifaqyl](https://github.com/arifaqyl)
