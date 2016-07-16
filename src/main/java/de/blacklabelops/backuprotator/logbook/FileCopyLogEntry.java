package de.blacklabelops.backuprotator.logbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.blacklabelops.backuprotator.Bucket;

public class FileCopyLogEntry extends AbstractMoveLogEntry {

	private final static Logger logger = LoggerFactory.getLogger(FileCopyLogEntry.class);

	private final static String logFormatString = "COPY {} FROM /{}/{} TO /{}/{}";

	private final static String textFormatString = "COPY %s FROM /%s/%s TO /%s/%s";

	public FileCopyLogEntry(Bucket sourceBucket, Bucket targetBucket, String filename) {
		super(sourceBucket, targetBucket, filename);
		logEntry();
	}

	@Override
	public void logEntry() {
		logger.debug(logFormatString, filename, sourceBucket.getName(), sourceBucket.getPath(), targetBucket.getName(),
				targetBucket.getPath());
	}

	@Override
	public String getMessage() {
		return String.format(textFormatString, filename, sourceBucket.getName(), sourceBucket.getPath(),
				targetBucket.getName(), targetBucket.getPath());
	}

}
