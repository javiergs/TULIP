package javiergs.tulip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Lightweight helper for accessing the GitHub REST API (v3),
 * supporting basic operations such as retrieving file content
 * and listing files or folders in a repository.
 *
 * Use with or without a token. Anonymous: ~60 req/hr; token: ~5000 req/hr.
 *
 * @author Javier Gonzalez-Sanchez
 * @version 2.0
 */
public class GitHubHandler {
	
	private static final String API_BASE = "https://api.github.com";
	private String token;
	
	/**
	 * Default constructor without authentication token.
	 * Use this for anonymous access (limited rate).
	 * GitHub allows anonymous access with a limited rate (60 req/hr).
	 */
	public GitHubHandler() {
		this.token = null;
	}
	
	/**
	 * Constructor with authentication token.
	 * Use this for higher rate limits (5000 req/hr).
	 * @param token GitHub personal access token (PAT) or OAuth token.
	 *              If null or empty, anonymous access is used.
	 */
	public GitHubHandler(String token) {
		this.token = token;
	}
	
	/**
	 * Get decoded text content of a file via URL.
	 *
	 * @param fileUrl the full URL to the file on GitHub.
	 *
	 * @return the decoded text content of the file
	 *
	 * @throws IllegalArgumentException if the URL does not point to a file
	 * @throws IOException if the file cannot be accessed or decoded
	 */
	public String getFileContentFromUrl(String fileUrl) throws IOException {
		URLHelper u = URLHelper.parseGitHubUrl(fileUrl);
		if (!u.isBlob) {
			throw new IllegalArgumentException("URL does not point to a file (/blob/...): " + fileUrl);
		}
		String path = (u.path == null) ? "" : u.path;
		return getFileContent(u.owner, u.repo, path, u.ref);
	}
	
	/**
	 * Get decoded text content of a file via owner/repo/path (+ optional ref).
	 * The file must be a text file (not binary).
	 * The content is returned as a UTF-8 string.
	 *
	 * @param owner the repository owner (user or organization) must not be null/blank
	 * @param repo  the repository name must not be null/blank
	 * @param path  the file path within the repository; if null or blank, the repository root is used
	 * @param ref   optional branch/tag/commit; if null or blank, the repository’s default branch is used
	 *
	 * @return the file content decoded as UTF-8 text
	 *
	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if any argument is invalid or the path does not reference a file
	 
	 */
	public String getFileContent(String owner, String repo, String path, String ref) throws IOException {
		StringBuilder url = new StringBuilder();
		url.append(API_BASE).append("/repos/")
			.append(enc(owner)).append("/").append(enc(repo))
			.append("/contents/").append(enc(path));
		if (ref != null && !ref.isBlank()) {
			url.append("?ref=").append(encQuery(ref));
		}
		JSONObject fileObj = new JSONObject(apiGet(url.toString()));
		String encoding = fileObj.optString("encoding", "");
		if (!"base64".equalsIgnoreCase(encoding)) {
			throw new IOException("Unexpected encoding for file content: " + encoding);
		}
		String encoded = fileObj.getString("content").replaceAll("\\s", "");
		byte[] decoded = java.util.Base64.getDecoder().decode(encoded);
		return new String(decoded, StandardCharsets.UTF_8);
	}
	
	/**
	 * List files (names only) in a directory by URL (non-recursive).
	 *
	 * @param dirUrl the full URL to the directory on GitHub.
	 *
	 * @return a list of file names (not full paths) in the specified directory
	 *
	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if the URL does not point to a directory
	 * @throws IllegalArgumentException if the URL points to a file instead of a directory
	 *
	 */
	public List<String> listFiles(String dirUrl) throws IOException {
		URLHelper u = URLHelper.ensureTree(dirUrl);
		return listFiles(u.owner, u.repo, u.path, u.ref); // path first, then ref
	}
	
	/**
	 * Lists files in a directory by owner/repo/path (+ optional ref).
	 * The returned paths can be passed directly as the {@code path} argument to
	 * {@link #getFileContent(String, String, String, String)}.
	 *
	 * @param owner the repository owner (user or organization) must not be null/blank
	 * @param repo  the repository name must not be null/blank
	 * @param path  the directory path within the repository; if null or blank, the repository root is used
	 * @param ref   optional branch/tag/commit; if null or blank, the repository’s default branch is used
	 *
	 * @return a list of file names (full paths) in the specified directory
	 *
	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if any argument is invalid or the path does not reference a directory
	 */
	public List<String> listFiles(String owner, String repo, String path, String ref) throws IOException {
		JSONArray arr = getContentsArray(owner, repo, path, ref);
		List<String> files = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			if ("file".equals(o.getString("type"))) {
				files.add(o.getString("path"));
			}
		}
		return files;
	}
	
	/**
	 * List files (names only) in a directory by URL (non-recursive).
	 * This method is similar to {@link #listFiles(String, String, String, String)},
	 * but it takes a URL instead of owner/repo/branch/path.
	 * @param dirUrl the full URL to the directory on GitHub.
	 *
	 * @return a list of file names (not full paths) in the specified directory
	 *
	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if the URL does not point to a directory
	 * @throws IllegalArgumentException if the URL points to a file instead of a directory
	 */
	public List<String> listFolders(String dirUrl) throws IOException {
		URLHelper u = URLHelper.ensureTree(dirUrl);
		return listFolders(u.owner, u.repo, u.ref, u.path); // branch then path
	}
	
	/**
	 * This method retrieves the contents of a directory in a GitHub repository.
	 *
	 * @param owner the repository owner (user or organization) must not be null/blank
	 * @param repo the repository name must not be null/blank
	 * @param branch the branch name; if null or blank, the repository’s default branch is used
	 * @param path the directory path within the repository; if null or blank, the repository root is used

	 * @return a list of file names (not full paths) in the specified directory

	 * @throws IllegalArgumentException if any argument is invalid or the path does not reference a directory
	 * @throws IOException if the API call fails or the response cannot be decoded
	 */
	public List<String> listFolders(String owner, String repo, String branch, String path) throws IOException {
		JSONArray arr = getContentsArray(owner, repo, path, branch);
		List<String> folders = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			if ("dir".equals(o.getString("type"))) {
				folders.add(o.getString("path"));
			}
		}
		return folders;
	}
	
	/** URL-based: recursively list all files under a directory URL (or entire repo if root).
	 * This method is similar to {@link #listFilesRecursive(String, String, String, String)},
	 * but it takes a URL instead of owner/repo/branch/path.
	 *
	 * @param startDirUrl the full URL to the directory on GitHub.
	 *
	 * @return a list of file paths (relative to the repository root)
	 *
	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if the URL does not point to a directory
	 */
	public List<String> listFilesRecursive(String startDirUrl) throws IOException {
		URLHelper u = URLHelper.parseGitHubUrl(startDirUrl);
		if (u.isBlob) throw new IllegalArgumentException("URL points to a file, not a directory: " + startDirUrl);
		return listFilesRecursive(u.owner, u.repo, u.ref, u.path);
	}
	
	/** Recursively list all file paths (relative to repo root) under owner/repo/branch/path.
	 *
	 * @param owner the repository owner (user or organization) must not be null/blank
	 * @param repo the repository name must not be null/blank
	 * @param branch the branch name; if null or blank, the repository’s default branch is used
	 * @param path the directory path within the repository; if null or blank, the repository root is used

	 * @return a list of file paths (relative to the repository root)

	 * @throws IOException if the API call fails or the response cannot be decoded
	 * @throws IllegalArgumentException if any argument is invalid or the path does not reference a directory
	 *
	 */
	public List<String> listFilesRecursive(String owner, String repo, String branch, String path) throws IOException {
		List<String> results = new ArrayList<>();
		Deque<String> stack = new ArrayDeque<>();
		stack.push((path == null) ? "" : path);
		while (!stack.isEmpty()) {
			String subPath = stack.pop();
			JSONArray arr = getContentsArray(owner, repo, subPath, branch);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.getJSONObject(i);
				String type = o.getString("type"); // "file" | "dir" | "symlink" | "submodule"
				String filePath = o.getString("path");
				if ("file".equals(type)) {
					results.add(filePath);
				} else if ("dir".equals(type)) {
					stack.push(filePath);
				}
			}
		}
		return results;
	}
	
	private JSONArray getContentsArray(String owner, String repo, String path, String ref) throws IOException {
		StringBuilder url = new StringBuilder();
		url.append(API_BASE).append("/repos/")
			.append(enc(owner)).append("/").append(enc(repo))
			.append("/contents");
		if (path != null && !path.isBlank()) {
			url.append("/").append(enc(path));
		}
		if (ref != null && !ref.isBlank()) {
			url.append("?ref=").append(encQuery(ref));
		}
		String finalUrl = url.toString();
		String json = apiGet(finalUrl);
		return new JSONArray(json);
	}
	
	private String apiGet(String url)
		throws IOException {
		HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
		c.setRequestMethod("GET");
		c.setRequestProperty("Accept", "application/vnd.github.v3+json");
		c.setRequestProperty("User-Agent", "javiergs-tulip/2.0"); // GitHub requires a UA
		if (token != null && !token.isBlank()) {
			c.setRequestProperty("Authorization", "Bearer " + token.trim());
		}
		int status = c.getResponseCode();
		if (status != 200) {
			String rem = c.getHeaderField("X-RateLimit-Remaining");
			String rst = c.getHeaderField("X-RateLimit-Reset");
			throw new IOException("GitHub API failed: HTTP " + status
				+ (rem != null ? " (remaining=" + rem + ", resetEpoch=" + rst + ")" : ""));
		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) sb.append(line);
			return sb.toString();
		} finally {
			c.disconnect();
		}
	}
	
	private static String enc(String s) {
		String e = URLEncoder.encode(s, StandardCharsets.UTF_8);
		return e.replace("%2F", "/");
	}
	
	private static String encQuery(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
	
	private static String orDefault(String ref) {
		return ref;
	}
	
}