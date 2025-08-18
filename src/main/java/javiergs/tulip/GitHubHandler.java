package javiergs.tulip;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.Base64;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A utility class for interacting with public GitHub repositories using the GitHub REST API.
 * Supports listing files in a folder and retrieving file contents.
 *
 * @author Javier Gonzalez-Sanchez
 * @version 1.0
 */
public class GitHubHandler {
	
	private final String owner;
	private final String repo;
	
	/**
	 * Constructor to initialize the GitHubHandler with a specific repository.
	 * @param owner the GitHub username or organization
	 * @param repo  the name of the repository
	 */
	public GitHubHandler(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
	}
	
	/**
	 * Lists all file names in a given folder of a public GitHub repository.
	 *
	 * @param path  the folder path inside the repository
	 * @return a list of file paths (relative to the root)
	 * @throws IOException if the API call fails
	 */
	public List<String> listFiles(String path) throws IOException {
		String apiUrl = String.format(
			"https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path
		);
		JSONArray jsonArray = new JSONArray(get(apiUrl));
		List<String> files = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject fileObj = jsonArray.getJSONObject(i);
			if ("file".equals(fileObj.getString("type"))) {
				files.add(path + "/" + fileObj.getString("name"));
			}
		}
		return files;
	}
	
	/**
	 * Retrieves the raw content of a file in a public GitHub repository.
	 *
	 * @param path  the full file path inside the repository
	 * @return the decoded content of the file as a String
	 * @throws IOException if the file cannot be retrieved or is not base64-encoded
	 */
	public String getFileContent(String path) throws IOException {
		String apiUrl = String.format(
			"https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path
		);
		JSONObject fileObj = new JSONObject(get(apiUrl));
		String encoding = fileObj.optString("encoding");
		if (!"base64".equals(encoding)) {
			throw new IOException("Unexpected encoding: " + encoding);
		}
		String encoded = fileObj.getString("content").replaceAll("\\s", "");
		return new String(Base64.getDecoder().decode(encoded));
	}
	
	private String get(String apiUrl) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
		
		int status = connection.getResponseCode();
		if (status != 200) {
			throw new IOException("GitHub API call failed with status: " + status);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder contentBuilder = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			contentBuilder.append(inputLine);
		}
		in.close();
		connection.disconnect();
		return contentBuilder.toString();
	}
	
	/**
	 * Recursively lists all file names in a given folder of a public GitHub repository.
	 *
	 * @param path  the folder path inside the repository
	 * @return a list of file paths (relative to the root)
	 * @throws IOException if the API call fails
	 */
	public List<String> listFilesRecursive(String path) throws IOException {
		String apiUrl = String.format(
			"https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path
		);
		JSONArray jsonArray = new JSONArray(get(apiUrl));
		List<String> files = new ArrayList<>();
		
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject fileObj = jsonArray.getJSONObject(i);
			String type = fileObj.getString("type");
			String filePath = fileObj.getString("path"); // GitHub API returns full relative path
			
			if ("file".equals(type)) {
				files.add(filePath);
			} else if ("dir".equals(type)) {
				// Recurse into subfolder
				files.addAll(listFilesRecursive(filePath));
			}
		}
		return files;
	}
	
	
	/**
	 * Parses a GitHub URL, updates this object's owner/repo,
	 * and returns ALL files (recursive) under the parsed path.
	 *
	 * Supported URL forms:
	 *   https://github.com/{owner}/{repo}
	 *   https://github.com/{owner}/{repo}/tree/{branch}
	 *   https://github.com/{owner}/{repo}/tree/{branch}/{path...}
	 *   https://github.com/{owner}/{repo}/blob/{branch}/{path...}
	 *
	 * If no path is present after /tree/{branch}/ or /blob/{branch}/,
	 * the repository root is used.
	 *
	 * @param repoUrl a GitHub URL
	 * @return list of all file paths (recursive) starting at the parsed path (or repo root)
	 * @throws IOException on API failures
	 * @throws IllegalArgumentException if the URL cannot be parsed
	 */
	public List<String> listFilesFromUrl(String repoUrl) throws IOException {
		String path = parseGithubUrl(repoUrl);
		String startPath = (path == null) ? "" : path;
		return listFilesRecursive(startPath);
	}

	
	/**
	 * Parses a GitHub URL into owner, repo, and optional path (after /tree/{branch}/ or /blob/{branch}/).
	 */
	private String parseGithubUrl(String url) {
		String s = url.trim();
		if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
		URI uri = URI.create(s);
		if (uri.getHost() == null || !uri.getHost().equalsIgnoreCase("github.com")) {
			throw new IllegalArgumentException("Not a github.com URL: " + url);
		}
		String[] parts = uri.getPath().split("/");
		// path starts with '/', so parts[0] == ""
		if (parts.length < 3) {
			throw new IllegalArgumentException("URL must be like https://github.com/{owner}/{repo}[...]");
		}
		String owner = parts[1];
		String repo  = parts[2];
		String path = null;
		if (parts.length >= 5 && ("tree".equals(parts[3]) || "blob".equals(parts[3]))) {
			if (parts.length > 5) {
				StringBuilder sb = new StringBuilder();
				for (int i = 5; i < parts.length; i++) {
					if (sb.length() > 0) sb.append('/');
					sb.append(parts[i]);
				}
				path = sb.toString();
			} else {
				path = ""; // explicitly no path after branch
			}
		}
		return path;
	}

}