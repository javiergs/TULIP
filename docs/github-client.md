The GitHub utilities provide lightweight helpers for parsing GitHub URLs and reading remote repository contents through the GitHub API.

They support:

- GitHub URL parsing
- Repository root, tree, and blob URL handling
- Remote file retrieval
- File and folder listing
- Recursive repository traversal
- Optional GitHub token authentication

## URL Parsing

### URLFactory

`URLFactory` parses GitHub URLs and converts them into structured `URLObject` instances.

Supported URL types:

- repository root URLs
- tree URLs
- blob URLs

Example:

```java
URLObject u = URLFactory.parseGitHubUrl(
    "https://github.com/javiergs/ADASIM/tree/main/src"
);
```

### URLObject

`URLObject` represents a parsed GitHub resource.

It includes:

- owner
- repository
- revision
- path
- kind

Example:

```java
System.out.println(u.getOwner());
System.out.println(u.getRepository());
System.out.println(u.getRevision());
System.out.println(u.getPath());
System.out.println(u.getKind());
```

Convenience helpers:

```java
u.isBlob();
u.isDirectory();
```


## GitHub API Access

### GitHubHandler

`GitHubHandler` provides utility methods for interacting with remote GitHub repositories.

> [!NOTE]
> GitHub API require a token when rate limits are reached or when accessing private repositories.

## List Files

```java
GitHubHandler gh = new GitHubHandler();

List<String> files =
    gh.listFiles("https://github.com/javiergs/ADASIM");
```


## List Folders

```java
GitHubHandler gh = new GitHubHandler();

List<String> folders =
    gh.listFolders("https://github.com/javiergs/ADASIM");
```


## Recursive File Traversal

```java
GitHubHandler gh = new GitHubHandler();

List<String> files =
    gh.listFilesRecursive(
        "https://github.com/javiergs/ADASIM"
    );
```


## Fetch Remote File Contents

```java
GitHubHandler gh = new GitHubHandler();

String text = gh.getFileContentFromUrl(
    "https://github.com/javiergs/ADASIM/blob/main/pom.xml"
);
```


## Optional GitHub Token

For unauthenticated requests, GitHub may apply low rate limits.

To use authentication, create:

```text
src/main/resources/tulip.properties
```

Add:

```properties
GITHUB_TOKEN=ghp_your_token_here
```

The token is loaded locally by the application using the library.

> [!NOTE]
> Do not commit `tulip.properties` if it contains a real GitHub token.


## Example: Reading a Repository

```java
GitHubHandler gh = new GitHubHandler();

List<String> files =
    gh.listFilesRecursive(
        "https://github.com/javiergs/ADASIM"
    );

for (String file : files) {
    System.out.println(file);
}
```

---

## Example: Reading a Single File

```java
GitHubHandler gh = new GitHubHandler();

String pom =
    gh.getFileContentFromUrl(
        "https://github.com/javiergs/ADASIM/blob/main/pom.xml"
    );

System.out.println(pom);
```
