package javiergs.tulip;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the URLFactory class.
 *
 * @author javiergs
 * @version 3.0 (2025.11.29)
 */
class URLFactoryTest {
	
	private static final String OWNER = "javiergs";
	private static final String REPO  = "ADASIM";
	
	@Test
	void parseRootRepositoryUrl() {
		String url = "https://github.com/" + OWNER + "/" + REPO;
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.ROOT, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parseRootRepositoryUrlWithTrailingSlash() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.ROOT, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parsePlainUrlWithPathAssumesMainAndTree() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/src";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("src", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.TREE, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parsePlainUrlWithMultiSegmentPathAssumesMainAndTree() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/src/main/java";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("src/main/java", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.TREE, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parseTreeUrlWithBranchRoot() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/tree/main";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.TREE, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parseTreeUrlWithBranchAndPath() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/tree/main/src";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("src", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.TREE, urlObject.kind);
		Assertions.assertFalse(urlObject.isBlob());
		Assertions.assertTrue(urlObject.isDirectory());
	}
	
	@Test
	void parseBlobUrlForReadme() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/blob/main/README.md";
		URLObject urlObject = URLFactory.parseGitHubUrl(url);
		Assertions.assertEquals(OWNER, urlObject.owner);
		Assertions.assertEquals(REPO, urlObject.repository);
		Assertions.assertEquals("main", urlObject.revision);
		Assertions.assertEquals("README.md", urlObject.path);
		Assertions.assertEquals(URLObject.Kind.BLOB, urlObject.kind);
		Assertions.assertTrue(urlObject.isBlob());
		Assertions.assertFalse(urlObject.isDirectory());
	}
	
	@Test
	void parseInvalidHostShouldThrow() {
		String url = "https://example.com/" + OWNER + "/" + REPO;
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			URLFactory.parseGitHubUrl(url));
	}
	
	@Test
	void parseMissingRepositoryShouldThrow() {
		String url = "https://github.com/" + OWNER;
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			URLFactory.parseGitHubUrl(url));
	}
	
	@Test
	void parseBlobMissingPathShouldThrow() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/blob/main";
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			URLFactory.parseGitHubUrl(url));
	}
	
	@Test
	void parseTreeMissingRevisionShouldThrow() {
		String url = "https://github.com/" + OWNER + "/" + REPO + "/tree/";
		Assertions.assertThrows(IllegalArgumentException.class, () ->
			URLFactory.parseGitHubUrl(url));
	}

}