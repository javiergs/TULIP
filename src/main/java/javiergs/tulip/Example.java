package javiergs.tulip;

import java.util.List;

public class Example {
	public static void main(String[] args) throws Exception {
		GitHubHandler browser = new GitHubHandler();
		List<String> files = browser.listFiles("javiergs", "calpoly", "src/main/java");
		System.out.println("Files: " + files);
		
		if (!files.isEmpty()) {
			String content = browser.getFileContent("javiergs", "calpoly", files.get(0));
			System.out.println("Content of " + files.get(0) + ":\n" + content);
		}
	}
}