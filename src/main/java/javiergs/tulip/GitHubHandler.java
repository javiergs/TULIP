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
 * Fetches file contents and lists files/folders in GitHub repositories.
 *
 * @author javiergs
 * @version 3.0
 */
public class GitHubHandler {
	
	private static final String API_BASE = "https://api.github.com";
	private final String token;
	
	public GitHubHandler() {
		String t = loadTokenFromProperties();
		if (!t.isBlank())
			token = t;
		else
			token = null;
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
	
	public GitHubHandler(String token) {
		this.token = token;
	}
	
	public String getFileContentFromUrl(String fileUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(fileUrl);
		return getFileContent(u.owner, u.repository, u.path, u.revision);
	}
	
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
	
	public List<String> listFiles(String dirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(dirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory URL: " + dirUrl);
		}
		return listFiles(u.owner, u.repository, u.path, u.revision);
	}
	
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
	
	public List<String> listFolders(String dirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(dirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory URL: " + dirUrl);
		}
		return listFolders(u.owner, u.repository, u.revision, u.path);
	}
	
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
	
	public List<String> listFilesRecursive(String startDirUrl) throws IOException {
		URLObject u = URLFactory.parseGitHubUrl(startDirUrl);
		if (!u.isDirectory()) {
			throw new IllegalArgumentException("URL points to a file, not a directory: " + startDirUrl);
		}
		return listFilesRecursive(u.owner, u.repository, u.revision, u.path);
	}
	
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
		c.setRequestProperty("User-Agent", "javiergs-tulip/2.0"); // GitHub requires a UA
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

}