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

}