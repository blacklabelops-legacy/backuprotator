package de.blacklabelops.backuprotator.filehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.logbook.FileCopyLogEntry;
import de.blacklabelops.backuprotator.logbook.FileDeleteLogEntry;
import de.blacklabelops.backuprotator.logbook.FileMoveLogEntry;
import de.blacklabelops.backuprotator.logbook.Logbook;

public class LocalFileSystemHandler implements FileHandler {

	private final static Logger logger = LoggerFactory.getLogger(LocalFileSystemHandler.class);

	private final String root = System.getProperty("java.io.tmpdir");

	private boolean simulation = true;

	private LocalFileSystemHandler() {
		super();
	}

	public static LocalFileSystemHandler getInstance(boolean simulationMode) {
		LocalFileSystemHandler handler = new LocalFileSystemHandler();
		handler.simulation = simulationMode;
		return handler;
	}

	@Override
	public List<String> listFiles(Bucket bucket) {
		List<String> files = new ArrayList<String>();
		File sourceFolder = new File(root + bucket.getName(), bucket.getPath());
		for (File currentFile : sourceFolder.listFiles()) {
			files.add(currentFile.getName());
		}
		return files;
	}

	private String makePath(String bucket, String path, String fileName) {
		return root + bucket + File.separator + path + File.separator + fileName;
	}

	@Override
	public void copyFile(Bucket sourceBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileCopyLogEntry(sourceBucket, targetBucket, fileName));
		Path source = FileSystems.getDefault()
				.getPath(makePath(sourceBucket.getName(), sourceBucket.getPath(), fileName));
		Path target = FileSystems.getDefault()
				.getPath(makePath(targetBucket.getName(), targetBucket.getPath(), fileName));
		if (!simulation) {
			logger.trace("Not simulation , so moving files from " + source + " to " + target);
			try {
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error("File cannot be copied.", e);
				throw new RuntimeException(e);
			}
		} else {
			logger.trace("Simulation mode :Moving files from " + source + " to " + target);
		}
	}

	@Override
	public void moveFile(Bucket sourceBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileMoveLogEntry(sourceBucket, targetBucket, fileName));
		Path source = FileSystems.getDefault()
				.getPath(makePath(sourceBucket.getName(), sourceBucket.getPath(), fileName));
		Path target = FileSystems.getDefault()
				.getPath(makePath(targetBucket.getName(), targetBucket.getPath(), fileName));
		if (!simulation) {
			logger.trace("Not simulation , so moving files from " + source + " to " + target);
			try {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error("File cannot be moved.", e);
				throw new RuntimeException(e);
			}
		} else {
			logger.trace("Simulation mode Moving files from " + source + " to " + target);
		}
	}

	@Override
	public void deleteFile(Bucket bucket, String fileName, Logbook log) {
		log.addEntry(new FileDeleteLogEntry(bucket, fileName));
		Path source = FileSystems.getDefault().getPath(makePath(bucket.getName(), bucket.getPath(), fileName));
		if (!simulation) {
			try {
				Files.deleteIfExists(source);
			} catch (IOException e) {
				logger.error("File cannot be deleted", e);
				throw new RuntimeException(e);
			}
		} else {
			logger.trace("Simulation mode Cleaning up file :" + source);
		}
	}

}
