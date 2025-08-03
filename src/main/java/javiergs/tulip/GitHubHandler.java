package javiergs.tulip;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Base64;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubHandler {
	
	public List<String> listFiles(String owner, String repo, String path) throws IOException {
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
	
	public String getFileContent(String owner, String repo, String path) throws IOException {
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
		HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
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