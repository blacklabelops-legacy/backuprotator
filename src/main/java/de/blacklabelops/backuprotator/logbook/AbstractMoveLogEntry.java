package de.blacklabelops.backuprotator.logbook;

import de.blacklabelops.backuprotator.Bucket;

public abstract class AbstractMoveLogEntry implements ILogEntry {

	Bucket sourceBucket;

	Bucket targetBucket;

	String filename;

	public AbstractMoveLogEntry(Bucket sourceBucket, Bucket targetBucket, String filename) {
		super();
		this.sourceBucket = sourceBucket;
		this.targetBucket = targetBucket;
		this.filename = filename;
	}

}
