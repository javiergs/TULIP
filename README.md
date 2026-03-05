![Static Badge](https://img.shields.io/badge/author-javiergs-orange)
![GitHub repo size](https://img.shields.io/github/repo-size/javiergs/App-ProduceConsume)
[![](https://jitpack.io/v/javiergs/TULIP.svg)](https://jitpack.io/#javiergs/TULIP)
![Java](https://img.shields.io/badge/Java-21+-blue)

[![Build and deploy JavaDoc](https://github.com/javiergs/TULIP/actions/workflows/javadoc-pages.yml/badge.svg?branch=main&event=push)](https://github.com/javiergs/TULIP/actions/workflows/javadoc-pages.yml)

## Features

- GitHub API helpers for reading remote repository contents  
- URL parsing utilities for GitHub paths  
- Optional GitHub token support via `tulip.properties`  
- Simple integration with Maven  

## Installation

Include **TULIP** in your Java project using JitPack.

### 1. Add the JitPack repository

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

### 2. Add the dependency

```xml
<dependency>
  <groupId>com.github.javiergs</groupId>
  <artifactId>TULIP</artifactId>
  <version>v3.0</version>
</dependency>
```

## What TULIP Provides

### URL Parsing Utilities

- **URLFactory**  
  Parses GitHub URLs (root, tree, blob) and normalizes missing parts (e.g., default branch = `main`)

- **URLObject**  
  Represents a structured GitHub resource with:
  - owner  
  - repository  
  - revision (branch/tag/commit)  
  - path  
  - kind (`ROOT`, `TREE`, `BLOB`)  
  - convenience helpers `isBlob()`, `isDirectory()`

### GitHub API Tools

**GitHubHandler**
- List files and folders in any repo  
- Fetch file contents  
- Recursive traversal of remote repositories  
- Automatic fallback to `main` branch  
- Supports authentication via a *local properties file*

## Using a GitHub Token (optional but recommended)

Create a file:

```
src/main/resources/tulip.properties
```

Add:

```
GITHUB_TOKEN=ghp_your_token_here
```

This avoids API rate limits for unauthenticated users.

## Basic Usage Examples

### Parse a GitHub URL

```java
URLObject u = URLFactory.parseGitHubUrl(
    "https://github.com/javiergs/ADASIM/tree/main/src"
);
```

### List files in a repository

```java
GitHubHandler gh = new GitHubHandler();
List<String> files = gh.listFiles("https://github.com/javiergs/ADASIM");
```

### Fetch contents of a remote file

```java
String text = gh.getFileContentFromUrl(
    "https://github.com/javiergs/ADASIM/blob/main/pom.xml"
);
```

## API Reference
Full JavaDoc available at:

➡️ https://javiergs.github.io/TULIP/
