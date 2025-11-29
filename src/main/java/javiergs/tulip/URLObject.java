package javiergs.tulip;

/**
 * Represents a GitHub URL object with an owner, repository, revision, path, and kind.
 *
 * @author javiergs
 * @version 3.0
 */
public class URLObject {
	
	protected String owner;
	protected String repository;
	protected String revision; // it could be a branch or tag or commit value, or null
	protected String path; // it will be an empty "" value for root
	protected Kind kind;
	
	/**
	 * The kind of the URL object: ROOT, TREE (directory), or BLOB (file).
	 */
	public enum Kind {ROOT, TREE, BLOB}
	
	/**
	 * Constructs a URLObject with the specified parameters.
	 *
	 * @param owner      the owner of the repository
	 * @param repository the name of the repository
	 * @param revision   the revision (branch, tag, or commit) can be null
	 * @param path       the path within the repository can be null or empty for root
	 * @param kind       the kind of the URL object (ROOT, TREE, BLOB)
	 * @throws IllegalArgumentException if the owner or repository is null or blank
	 */
	public URLObject(
		String owner,
		String repository,
		String revision,
		String path,
		Kind kind
	) {
		if (owner == null || owner.isBlank()) {
			throw new IllegalArgumentException("owner cannot be null or blank");
		}
		if (repository == null || repository.isBlank()) {
			throw new IllegalArgumentException("repository cannot be null or blank");
		}
		this.owner = owner;
		this.repository = repository;
		this.revision = revision;
		this.path = (path == null) ? "" : path;
		this.kind = kind == null ? Kind.ROOT : kind;
	}
	
	/**
	 * Checks if the URL object represents a blob (file).
	 *
	 * @return true if the kind is BLOB, false otherwise
	 */
	public boolean isBlob() {
		return this.kind == Kind.BLOB;
	}
	
	/**
	 * Checks if the URL object represents a directory (tree or root).
	 *
	 * @return true if the kind is TREE or ROOT, false otherwise
	 */
	public boolean isDirectory() {
		return kind == Kind.TREE || kind == Kind.ROOT;
	}
	
	/**
	 * Returns a readable string representation of this URLObject, including
	 * an owner, repository, revision, path, and kind.
	 *
	 * @return a string representation of the URLObject
	 */
	@Override
	public String toString() {
		return "URLObject{" +
			"owner='" + owner + '\'' +
			", repository='" + repository + '\'' +
			", revision='" + revision + '\'' +
			", path='" + path + '\'' +
			", kind=" + kind +
			'}';
	}
	
}