package javiergs.tulip;

import java.net.URI;

/**
 * Parses a GitHub URL and returns a URLObject instance.
 * It supports:
 * <p>
 * - https://github.com/{owner}/{repository}
 * - https://github.com/{owner}/{repository}/tree/{revision}
 * - https://github.com/{owner}/{repository}/tree/{revision}/{path...}
 * - https://github.com/{owner}/{repository}/blob/{revision}/{path...}
 *
 * @author javiergs
 * @version 3.0
 */
public class URLFactory {
	
	/**
	 * Parses a GitHub URL that explicitly uses /tree/ or /blob/.
	 *
	 * @param url GitHub URL to parse
	 * @return URLObject with parsed components
	 * @throws IllegalArgumentException if the URL is not a valid /tree/ or /blob/ GitHub URL
	 */
	public static URLObject parseGitHubUrl(String url) {
		URI uri = URI.create(url);
		if (!"github.com".equalsIgnoreCase(uri.getHost())) {
			throw new IllegalArgumentException("Not a github.com URL: " + url);
		}
		String[] segments = uri.getPath().split("/", -1);
		if (segments.length < 3 || segments[1].isBlank() || segments[2].isBlank()) {
			throw new IllegalArgumentException("Missing owner/repository in URL: " + url);
		}
		String owner = segments[1];
		String repository = segments[2];
		// CASE 1 — plain repo URL → assume main
		if (segments.length == 3) {
			return new URLObject(owner, repository, "main", "", URLObject.Kind.ROOT);
		}
		// CASE 2 — explicit /tree/ or /blob/
		URLObject.Kind kind = calculateKind(segments[3]);
		if (kind == URLObject.Kind.TREE || kind == URLObject.Kind.BLOB) {
			if (segments.length < 5 || segments[4].isBlank()) {
				throw new IllegalArgumentException("Missing {revision} segment: " + url);
			}
			String revision = segments[4];
			String path = buildPath(segments, 5);
			if (kind == URLObject.Kind.BLOB && path.isBlank()) {
				throw new IllegalArgumentException(
					"Missing file path after /blob/{revision}/: " + url);
			}
			return new URLObject(owner, repository, revision, path, kind);
		}
		// CASE 3 — plain: owner/repo/...path → assume main + TREE
		String dirPath = buildPath(segments, 3);
		URLObject.Kind type = dirPath.isBlank() ? URLObject.Kind.ROOT : URLObject.Kind.TREE;
		return new URLObject(owner, repository, "main", dirPath, type);
	}
	
	private static String buildPath(String[] segments, int startIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < segments.length; i++) {
			if (segments[i] != null && !segments[i].isBlank()) {
				if (sb.length() > 0) sb.append('/');
				sb.append(segments[i]);
			}
		}
		return sb.toString();
	}
	
	private static URLObject.Kind calculateKind(String s) {
		if ("tree".equals(s)) return URLObject.Kind.TREE;
		if ("blob".equals(s)) return URLObject.Kind.BLOB;
		if (s == null || s.isBlank()) return URLObject.Kind.ROOT;
		return null;
	}
	
}