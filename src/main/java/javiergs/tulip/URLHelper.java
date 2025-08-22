package javiergs.tulip;

import java.net.URI;
import java.util.Arrays;

public class URLHelper {
	
	public final String owner;
	public final String repo;
	public final String ref;   // branch/tag/commit or null
	public final String path;  // "" for root
	public final String kind;  // "root" | "tree" | "blob"
	public final boolean isBlob;
	
	public URLHelper(String owner, String repo, String ref, String path, String kind, boolean isBlob) {
		this.owner = owner;
		this.repo = repo;
		this.ref = ref;
		this.path = (path == null) ? "" : path;
		this.kind = kind;
		this.isBlob = isBlob;
	}
	
	/**
	 * Parses a GitHub URL and returns a URLHelper instance.
	 * Supports:
	 *  - https://github.com/{owner}/{repo}
	 *  - https://github.com/{owner}/{repo}/tree/{ref}
	 *  - https://github.com/{owner}/{repo}/tree/{ref}/{path...}
	 *  - https://github.com/{owner}/{repo}/blob/{ref}/{path...}
   *
	 * @param url The GitHub URL to parse.
	 *
	 * @return A URLHelper instance containing the parsed information.
	 *
	 * @throws IllegalArgumentException if the URL is not a valid GitHub URL
	 *
	 */
	public static URLHelper parseGitHubUrl(String url) {
		URI u = URI.create(url);
		if (!"github.com".equalsIgnoreCase(u.getHost())) {
			throw new IllegalArgumentException("Not a github.com URL: " + url);
		}
		
		String[] seg = u.getPath().split("/", -1); // keep empties
		// seg[0] = "", seg[1]=owner, seg[2]=repo, seg[3]=maybe kind
		if (seg.length < 3 || seg[1].isBlank() || seg[2].isBlank()) {
			throw new IllegalArgumentException("Missing owner/repo in URL: " + url);
		}
		
		String owner = seg[1];
		String repo  = seg[2];
		
		// Repo root (no tree/blob)
		if (seg.length == 3) {
			return new URLHelper(owner, repo, null, "", "root", false);
		}
		
		String kind = seg[3];
		if (!"tree".equals(kind) && !"blob".equals(kind)) {
			throw new IllegalArgumentException("Expected /tree/{ref}/… or /blob/{ref}/…: " + url);
		}
		
		if (seg.length < 5 || seg[4].isBlank()) {
			throw new IllegalArgumentException("Missing {ref} segment: " + url);
		}
		String ref = seg[4];
		
		// Build path from seg[5..]
		StringBuilder sb = new StringBuilder();
		for (int i = 5; i < seg.length; i++) {
			String s = seg[i];
			if (s != null && !s.isBlank()) {
				if (sb.length() > 0) sb.append('/');
				sb.append(s);
			}
		}
		String path = sb.toString();
		
		boolean isBlob = "blob".equals(kind);
		if (isBlob && path.isBlank()) {
			throw new IllegalArgumentException("Missing path segment after /blob/{ref}/ in: " + url);
		}
		
		return new URLHelper(owner, repo, ref, path, kind, isBlob);
	}
	

	
	/**
	 * Ensures that the provided GitHub URL refers to a directory (a tree or repo root),
	 * not to a single file (blob).
	 * <p>
	 * Parses the URL into a {@link URLHelper} object and validates that it points to
	 * a directory. If the URL points to a file, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param dirUrl a GitHub URL expected to represent a directory (tree) or repository root
	 * @return a {@link URLHelper} object containing the parsed components of the URL
	 * @throws IllegalArgumentException if the URL points to a file (blob) instead of a directory
	 */
	static URLHelper ensureTree(String dirUrl) {
		URLHelper u = parseGitHubUrl(dirUrl);
		if (u.isBlob) {
			throw new IllegalArgumentException("URL points to a file, not a directory: " + dirUrl);
		}
		// Keep owner/repo/ref/path exactly as parsed.
		return u;
	}
	
	// In URLHelper.java
	public URLHelper(String owner, String repo, String ref, String path, boolean isBlob) {
		this(owner, repo, /*kind*/ (isBlob ? "blob" : "tree"), ref, path, isBlob);
	}
	
}
