The `TaigaClient` module provides a lightweight Java wrapper around the Taiga REST API.

It supports:

- Authentication
- Project retrieval
- Sprint retrieval
- User story retrieval
- Task retrieval
- Creation/update/deletion operations
- Task status movement
- Pagination handling

## Authentication

Taiga integration requires a local configuration file.

Create:

```text
src/main/resources/tulip.properties
```

Add:

```properties
taiga.host=https://api.taiga.io
taiga.username=your_username
taiga.password=your_password
```

## Basic Authentication Example

```java
Properties p = new Properties();

try (InputStream in =
         getClass().getClassLoader()
             .getResourceAsStream("tulip.properties")) {

    p.load(in);
}

TaigaClient taiga = new TaigaClient(
    p.getProperty("taiga.host")
);

taiga.login(
    p.getProperty("taiga.username"),
    p.getProperty("taiga.password")
);
```


## Retrieve Projects

```java
List<TaigaProject> projects =
    taiga.getMyProjects();

for (TaigaProject p : projects) {
    System.out.println(p.getName());
}
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


## Pagination Support

The client automatically handles paginated Taiga API endpoints internally.


## PATCH Version Handling

Taiga requires PATCH operations to include the current object version.

The client automatically retrieves and injects the proper version field before updates.


> [!NOTE]
> The Taiga public API applies throttling/rate limits.
> Some heavy traversal integration tests are intentionally disabled to avoid excessive API requests.

