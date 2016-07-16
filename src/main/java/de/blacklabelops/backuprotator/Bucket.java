package de.blacklabelops.backuprotator;

public class Bucket {

	private String bucketName = null;

	private String bucketPath = null;

	public Bucket(String bucketName, String bucketPath) {
		super();
		this.bucketName = bucketName;
		this.bucketPath = bucketPath;
	}

	public String getName() {
		return bucketName;
	}

	public void setName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getPath() {
		return bucketPath;
	}

	public void setPath(String bucketPath) {
		this.bucketPath = bucketPath;
	}

}
