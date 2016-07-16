package de.blacklabelops.backuprotator.filehandler;

import java.util.List;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.logbook.Logbook;

public interface FileHandler {

	public List<String> listFiles(Bucket bucket);

	public void copyFile(Bucket sourceBucket, Bucket targetBucket, String fileName, Logbook log);

	public void moveFile(Bucket sourceBucket, Bucket targetBucket, String fileName, Logbook log);

	public void deleteFile(Bucket bucket, String fileName, Logbook log);

}