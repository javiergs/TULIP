![Static Badge](https://img.shields.io/badge/author-javiergs-orange)
![Version](https://img.shields.io/badge/version-v8.0-green)
[![](https://jitpack.io/v/javiergs/TULIP.svg)](https://jitpack.io/#javiergs/TULIP)
![GitHub repo size](https://img.shields.io/github/repo-size/javiergs/TULIP)
![Java](https://img.shields.io/badge/Java-21+-blue)
[![Build and deploy JavaDoc](https://github.com/javiergs/TULIP/actions/workflows/javadoc-pages.yml/badge.svg?branch=main&event=push)](https://github.com/javiergs/TULIP/actions/workflows/javadoc-pages.yml)

TULIP is a lightweight Java utility library focused on software engineering automation, repository analysis, educational tooling, and project-management integration.

Version 8.0 expands the library with a complete Taiga API client while preserving all previous GitHub repository utilities and URL parsing tools.


# Features

## GitHub Repository Utilities

- Parse GitHub repository URLs
- Read remote repository contents
- Traverse remote repositories recursively
- Fetch remote file contents
- Automatic fallback to `main` branch
- Optional GitHub token support via `tulip.properties`
- Lightweight Java-only implementation
- Maven-friendly integration

## URL Parsing Utilities

- Parse:
    - repository root URLs
    - tree URLs
    - blob URLs
- Normalize missing revisions
- Detect URL type automatically
- Extract:
    - owner
    - repository
    - branch/tag/commit
    - remote path

## Taiga API Client (NEW in v8.0)

- Authenticate with Taiga API
- Retrieve projects, sprints, stories, and tasks
- Create, update, and delete:
    - Projects
    - Sprints
    - User Stories
    - Tasks
- Move tasks and stories between statuses
- Pagination support
- Integration-tested implementation
- Automatic PATCH version handling


# Installation

Include **TULIP** in your Java project using JitPack.

## 1. Add the JitPack repository

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

## 2. Add the dependency

```xml
<dependency>
  <groupId>com.github.javiergs</groupId>
  <artifactId>TULIP</artifactId>
  <version>v8.0</version>
</dependency>
```

## 3. Using Taiga Authentication

Taiga integration requires a local configuration file containing your Taiga credentials.

Create:

```text
src/main/resources/tulip.properties
```

Add:

```text
TAIGA_HOST=https://api.taiga.io
TAIGA_USERNAME=your_username
TAIGA_PASSWORD=your_password
```

Example usage:

```java
Properties p = new Properties();

try (InputStream in =
         getClass().getClassLoader()
             .getResourceAsStream("tulip.properties")) {

    p.load(in);
}

TaigaClient taiga = new TaigaClient(
    p.getProperty("TAIGA_HOST")
);

taiga.login(
    p.getProperty("TAIGA_USERNAME"),
    p.getProperty("TAIGA_PASSWORD")
);
```

This keeps credentials outside source code and simplifies local development and testing.

# What TULIP Provides

# URL Parsing Utilities

## URLFactory

Parses GitHub URLs (`root`, `tree`, `blob`) and normalizes missing elements such as default branches.

### Example

```java
URLObject u = URLFactory.parseGitHubUrl(
    "https://github.com/javiergs/ADASIM/tree/main/src"
);
```


## URLObject

Represents a structured GitHub resource with:

- owner
- repository
- revision (branch/tag/commit)
- path
- kind (`ROOT`, `TREE`, `BLOB`)

Convenience helpers:

```java
u.isBlob();
u.isDirectory();
```


# GitHub API Tools

## GitHubHandler

Utilities for interacting with remote GitHub repositories.

### Supported Operations

- List files and folders in repositories
- Fetch remote file contents
- Recursive repository traversal
- Automatic fallback to `main` branch
- Optional token-based authentication


## List Files in a Repository

```java
GitHubHandler gh = new GitHubHandler();

List<String> files =
    gh.listFiles("https://github.com/javiergs/ADASIM");
```


## Fetch Contents of a Remote File

```java
String text = gh.getFileContentFromUrl(
    "https://github.com/javiergs/ADASIM/blob/main/pom.xml"
);
```


## Recursive Repository Traversal

```java
List<String> allFiles =
    gh.listFilesRecursive(
        "https://github.com/javiergs/ADASIM"
    );
```


# Using a GitHub Token (optional but recommended)

Create:

```text
src/main/resources/tulip.properties
```

Add:

```text
GITHUB_TOKEN=ghp_your_token_here
```

This helps avoid GitHub API rate limits for unauthenticated users.


# Taiga API Client

## TaigaClient

The new `TaigaClient` provides a lightweight Java wrapper around the Taiga REST API.


## Authentication

```java
TaigaClient taiga =
    new TaigaClient("https://api.taiga.io");

taiga.login(username, password);
```


## Retrieve Projects

```java
List<TaigaProject> projects =
    taiga.getMyProjects();
```


## Retrieve Sprints

```java
List<TaigaSprint> sprints =
    taiga.getSprints(projectId);
```


## Retrieve User Stories

```java
List<TaigaUserStory> stories =
    taiga.getStories(projectId);
```


## Retrieve Tasks

```java
List<TaigaTask> tasks =
    taiga.getTasks(projectId);
```


## Create Sprint

```java
TaigaSprint sprint = taiga.createSprint(
    projectId,
    "Sprint 1",
    "2026-06-01",
    "2026-06-14"
);
```


## Create User Story

```java
TaigaUserStory story =
    taiga.createUserStory(
        projectId,
        "Implement authentication",
        "User login support",
        sprint.getId()
    );
```


## Create Task

```java
TaigaTask task =
    taiga.createTask(
        projectId,
        story.getId(),
        "Implement REST client",
        null
    );
```


## Update Task

```java
TaigaTask updated =
    taiga.updateTask(
        task.getId(),
        "Updated task title",
        null,
        null
    );
```


## Move Task to Another Status

```java
TaigaTask moved =
    taiga.moveTaskToStatus(
        task.getId(),
        newStatusId
    );
```


## Delete Task

```java
taiga.deleteTask(task.getId());
```


# Notes on Taiga Integration Tests

The Taiga public API applies throttling/rate limits.

Some heavy traversal integration tests are intentionally disabled to avoid excessive API requests.

Integration tests require:

```text
src/test/resources/config.properties
```

Example:

```text
taiga.host=https://api.taiga.io
taiga.username=your_username
taiga.password=your_password
```


# API Reference

Full JavaDoc available at:

➡️ https://javiergs.github.io/TULIP/
