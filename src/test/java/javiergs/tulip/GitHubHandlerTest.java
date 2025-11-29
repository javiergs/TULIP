package javiergs.tulip;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;


/**
 * Integration tests for the GitHubHandler class.
 * These tests make real requests to GitHub's REST API and therefore require network access.
 * <p>
 * The GitHub access token is loaded exclusively from a tulip.properties file
 * located on the application classpath. The file must contain the property:
 * GITHUB_TOKEN = YOUR_TOKEN_HERE
 *
 * @author javiergs
 * @version 3.0 (2025.11.29)
 */
@Tag("integration")
class GitHubHandlerTest {
	
	private static GitHubHandler gitHubHandler;
	
	private static final String OWNER = "javiergs";
	private static final String REPOSITORY = "ADASIM";
	private static final String BRANCH = "main";
	
	@BeforeAll
	static void setup() {
		gitHubHandler = new GitHubHandler();
	}
	
	@Test
	@DisplayName("listFolders(repository root) should include 'library' when it exists")
	void listFolders_repoRoot_includesLibrary_ifPresent() throws IOException {
		List<String> folders = gitHubHandler.listFolders(OWNER, REPOSITORY, BRANCH, "");
		Assertions.assertNotNull(folders);
		Assertions.assertTrue(folders.contains("library"),
			"Expected 'library' among top-level directories of ADASIM");
	}
	
	@Test
	@DisplayName("listFiles(repository root) should include pom.xml at repository root")
	void listFiles_repoRoot_containsPomXml() throws IOException {
		List<String> files = gitHubHandler.listFiles(OWNER, REPOSITORY, "", BRANCH);
		Assertions.assertNotNull(files);
		Assertions.assertTrue(files.contains("pom.xml"),
			"Expected 'pom.xml' among top-level files of ADASIM");
	}
	
	@Test
	@DisplayName("listFilesRecursive(repository root) should eventually include pom.xml")
	void listFilesRecursive_root_includesPomXml() throws IOException {
		List<String> all = gitHubHandler.listFilesRecursive(OWNER, REPOSITORY, BRANCH, "");
		Assertions.assertNotNull(all);
		Assertions.assertTrue(all.contains("pom.xml"),
			"Recursive listing should include 'pom.xml' somewhere");
	}
	
	@Test
	@DisplayName("getFileContent(pom.xml) returns Maven XML with <project> tag")
	void getFileContent_pom_containsProjectTag() throws IOException {
		String text = gitHubHandler.getFileContent(OWNER, REPOSITORY, "pom.xml", BRANCH);
		Assertions.assertNotNull(text);
		Assertions.assertFalse(text.isBlank());
		Assertions.assertTrue(text.contains("<project"),
			"pom.xml should contain a <project> element");
	}
	
	@Nested
	@DisplayName("URL-based helpers against ADASIM")
	class UrlBased {
		
		@Test
		@DisplayName("listFiles(url to repository root) returns pom.xml")
		void listFiles_url_repoRoot_includesPomXml() throws IOException {
			String url = "https://github.com/javiergs/ADASIM";
			List<String> files = gitHubHandler.listFiles(url);
			Assertions.assertNotNull(files);
			Assertions.assertTrue(files.contains("pom.xml"),
				"Expected 'pom.xml' from URL-based listing of ADASIM root");
		}
		
		@Test
		@DisplayName("listFolders(url to repository root) includes 'library'")
		void listFolders_url_root_includesLibrary() throws IOException {
			String url = "https://github.com/javiergs/ADASIM";
			List<String> folders = gitHubHandler.listFolders(url);
			Assertions.assertNotNull(folders);
			Assertions.assertTrue(folders.contains("library"),
				"Expected 'library' among top-level directories via URL");
		}
		
		@Test
		@DisplayName("listFilesRecursive(url to /tree/main) includes pom.xml")
		void listFilesRecursive_url_treeMain_includesPomXml() throws IOException {
			String url = "https://github.com/javiergs/ADASIM/tree/main";
			List<String> all = gitHubHandler.listFilesRecursive(url);
			Assertions.assertNotNull(all);
			Assertions.assertTrue(all.contains("pom.xml"),
				"Recursive listing from /tree/main should include 'pom.xml'");
		}
		
		@Test
		@DisplayName("getFileContentFromUrl(blob pom.xml) returns non-empty text")
		void getFileContentFromUrl_blobPomXml_returnsText() throws IOException {
			String url = "https://github.com/javiergs/ADASIM/blob/main/pom.xml";
			String text = gitHubHandler.getFileContentFromUrl(url);
			Assertions.assertNotNull(text);
			Assertions.assertTrue(text.length() > 20,
				"Expected pom.xml content length > 20 characters");
		}
	}
	
	@Test
	@DisplayName("parseGitHubUrl: blob missing path should throw")
	void parseBlobMissingPath() {
		Exception ex = Assertions.assertThrows(IllegalArgumentException.class, () ->
			URLFactory.parseGitHubUrl("https://github.com/javiergs/ADASIM/blob/main")
		);
		String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
		Assertions.assertTrue(msg.contains("missing") || msg.contains("path"),
			"Expected message to mention missing path");
	}
	
	@Test
	@DisplayName("/tree/main (root) is parsed as directory, not blob")
	void treeMain_root_isDirectory_notBlob() {
		String url = "https://github.com/javiergs/ADASIM/tree/main";
		URLObject u = URLFactory.parseGitHubUrl(url);
		Assertions.assertTrue(u.isDirectory(), "Expected a directory URL: " + url);
		Assertions.assertEquals("", u.path, "Root path should be empty string");
		Assertions.assertFalse(u.isBlob(), "Tree URL for root should not be marked as blob");
	}
	
}