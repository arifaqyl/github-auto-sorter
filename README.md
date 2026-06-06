# GitHub Profile Organizer

Automatically organizes GitHub repository topics and metadata using the GitHub REST API.

Built with Java 11 — no frameworks, no fluff.

## What it does

- Fetches all public repositories for any GitHub username
- Analyses each repo's name, description, and primary language
- Suggests relevant topics (e.g. `java`, `algorithms`, `web`, `automation`)
- Optionally applies the tags directly via the GitHub API
- Can read a fine-grained token from `GITHUB_TOKEN`, `GITHUB_PAT`, or `GH_TOKEN`

## Usage

**Preview mode** (no token needed — just shows suggestions):
```bash
java -jar github-profile-organizer.jar
# Enter username, leave token blank
```

**Apply mode** (requires a GitHub Personal Access Token):
```bash
java -jar github-profile-organizer.jar
# Enter username + PAT, or set GITHUB_TOKEN / GITHUB_PAT / GH_TOKEN
```

## Building

```bash
mvn clean package
java -jar target/github-profile-organizer.jar
```

## Getting a Personal Access Token

1. GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens
2. Create token with **Repository** → **Metadata** (Read and Write)
3. Add access to the repos you want to organize
4. Paste when prompted, or export it as `GITHUB_TOKEN`

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
