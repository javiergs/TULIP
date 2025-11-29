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
 * Lists files and folders in GitHub repositories and fetches file contents
 * via the GitHub REST API.
 *
 * @author javiergs
 * @version 3.0
 */
public class GitHubHandler {
	
	private static final String API_BASE = "https://api.github.com";
	private final String token;
	
	/**
	 * Constructs a GitHubHandler that loads a GITHUB_TOKEN
	 * from the tulip.properties file in the application classpath.
	 */
	public GitHubHandler() {
		String t = loadTokenFromProperties();
		if (!t.isBlank())
			token = t;
		else
			token = null;
	}
	
	/**
	 * Constructs a GitHubHandler using the specified token.
	 *
	 * Normally, the token should be provided through the tulip.properties file.
	 * This constructor is intended for programmatic use cases where the token must be supplied manually.
	 *
	 * @param token GitHub access token used for authenticated API requests
	 */
	public GitHubHandler(String token) {
		this.token = token;
	}
	
	/**
	 * Fetches the content of a file from a GitHub URL.
	 *
	 * @param fileUrl URL of the file in the GitHub repository
	 * @return Content of the file as a String
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public String getFileContentFromUrl(String fileUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(fileUrl);
		return getFileContent(u.owner, u.repository, u.path, u.revision);
	}
	
	/**
	 * Fetches the content of a file from a GitHub repository.
	 *
	 * @param owner Repository owner
	 * @param repo  Repository name
	 * @param path  Path to the file within the repository
	 * @param ref   Branch, tag, or commit SHA (can be null to use default branch)
	 * @return Content of the file as a String
	 * @throws IOException if an I/O error occurs during the API request
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
	 * Lists files in a GitHub directory URL.
	 *
	 * @param dirUrl URL of the directory in the GitHub repository
	 * @return List of file paths within the specified directory
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public List<String> listFiles(String dirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(dirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory URL: " + dirUrl);
		}
		return listFiles(u.owner, u.repository, u.path, u.revision);
	}
	
	/**
	 * Lists files in a GitHub repository directory.
	 *
	 * @param owner Repository owner
	 * @param repo  Repository name
	 * @param path  Path to the directory within the repository
	 * @param ref   Branch, tag, or commit SHA (can be null to use default branch)
	 * @return List of file paths within the specified directory
	 * @throws IOException if an I/O error occurs during the API request
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
	 * Lists folders in a GitHub directory URL.
	 *
	 * @param dirUrl URL of the directory in the GitHub repository
	 * @return List of folder paths within the specified directory
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public List<String> listFolders(String dirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(dirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory URL: " + dirUrl);
		}
		return listFolders(u.owner, u.repository, u.revision, u.path);
	}
	
	/**
	 * Lists folders in a GitHub repository directory.
	 *
	 * @param owner Repository owner
	 * @param repo  Repository name
	 * @param path  Path to the directory within the repository
	 * @param ref   Branch, tag, or commit SHA (can be null to use default branch)
	 * @return List of folder paths within the specified directory
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public List<String> listFolders(String owner, String repo, String ref, String path) throws IOException {
		JSONArray arr = getContentsArray(owner, repo, path, ref);
		List<String> folders = new ArrayList<>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject o = arr.getJSONObject(i);
			if ("dir".equals(o.getString("type"))) {
				folders.add(o.getString("path"));
			}
		}
		return folders;
	}
	
	/**
	 * Recursively lists all files in a GitHub directory URL and its subdirectories.
	 *
	 * @param startDirUrl URL of the starting directory in the GitHub repository
	 * @return List of all file paths within the directory and its subdirectories
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public List<String> listFilesRecursive(String startDirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(startDirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("URL points to a file, not a directory: " + startDirUrl);
		}
		return listFilesRecursive(u.owner, u.repository, u.revision, u.path);
	}
	
	/**
	 * Recursively lists all files in a GitHub repository directory and its subdirectories.
	 *
	 * @param owner Repository owner
	 * @param repo  Repository name
	 * @param path  Path to the directory within the repository
	 * @param ref   Branch, tag, or commit SHA (can be null to use default branch)
	 * @return List of all file paths within the directory and its subdirectories
	 * @throws IOException if an I/O error occurs during the API request
	 */
	public List<String> listFilesRecursive(String owner, String repo, String ref, String path) throws IOException {
		List<String> results = new ArrayList<>();
		Deque<String> stack = new ArrayDeque<>();
		stack.push((path == null) ? "" : path);
		
		while (!stack.isEmpty()) {
			String subPath = stack.pop();
			JSONArray arr = getContentsArray(owner, repo, subPath, ref);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject o = arr.getJSONObject(i);
				String type = o.getString("type");  // "file" | "dir" | "symlink" | "submodule"
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
		String json = apiGet(url.toString());
		return new JSONArray(json);
	}
	
	private String apiGet(String url) throws IOException {
		HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
		c.setRequestMethod("GET");
		c.setRequestProperty("Accept", "application/vnd.github.v3+json");
		c.setRequestProperty("User-Agent", "javiergs-tulip/3.0");
		if (token != null && !token.isBlank()) {
			c.setRequestProperty("Authorization", "Bearer " + token.trim());
		}
		int status = c.getResponseCode();
		if (status != 200) {
			String remaining = c.getHeaderField("X-RateLimit-Remaining");
			boolean rateLimited = "0".equals(remaining);
			String baseMsg = "GitHub API request failed with HTTP " + status;
			if (rateLimited && (token == null || token.isBlank())) {
				throw new IOException("GitHub API access failed. Provide a valid GITHUB_TOKEN environment variable.", null);
			}
			throw new IOException(baseMsg + ". Remaining=" + remaining + ", Reset=" +
				c.getHeaderField("X-RateLimit-Reset"));
		}
		try (BufferedReader in = new BufferedReader(
			new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
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
	
	private String loadTokenFromProperties() {
		try {
			var in = getClass().getClassLoader().getResourceAsStream("tulip.properties");
			if (in == null) return null;
			Properties props = new Properties();
			props.load(in);
			String t = props.getProperty("GITHUB_TOKEN");
			return (t == null || t.isBlank()) ? null : t.trim();
		} catch (Exception e) {
			return null;
		}
	}
	
}