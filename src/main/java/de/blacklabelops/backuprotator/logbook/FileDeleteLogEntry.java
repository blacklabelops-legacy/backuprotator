package de.blacklabelops.backuprotator.logbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.blacklabelops.backuprotator.Bucket;

public class FileDeleteLogEntry implements ILogEntry {

	private final static Logger logger = LoggerFactory.getLogger(FileDeleteLogEntry.class);

	private final static String logFormatString = "DELETE {} FROM /{}/{}";

	private final static String textFormatString = "DELETE %s FROM /%s/%s";

	private Bucket bucket;

	private String filename;

	public FileDeleteLogEntry(Bucket bucket, String filename) {
		super();
		this.bucket = bucket;
		this.filename = filename;
	}

	@Override
	public void logEntry() {
		logger.debug(logFormatString, filename, bucket.getName(), bucket.getPath());

	}

	@Override
	public String getMessage() {
		return String.format(textFormatString, filename, bucket.getName(), bucket.getPath());
	}

}
