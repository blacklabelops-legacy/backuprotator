package de.blacklabelops.backuprotator.filehandler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectListing;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.logbook.FileCopyLogEntry;
import de.blacklabelops.backuprotator.logbook.FileDeleteLogEntry;
import de.blacklabelops.backuprotator.logbook.FileMoveLogEntry;
import de.blacklabelops.backuprotator.logbook.Logbook;
import de.blacklabelops.backuprotator.util.Setting;

public class AWSHandler implements FileHandler {

	private static final String SEPARATOR = "/";

	private final static Logger logger = LoggerFactory.getLogger(AWSHandler.class);

	private AmazonS3Client s3Client;

	private boolean simulation = true;

	private AWSHandler() {
		super();
	}

	public static AWSHandler getInstance(boolean simulationMode) {
		AWSHandler handler = new AWSHandler();
		handler.simulation = simulationMode;
		BasicAWSCredentials cred = new BasicAWSCredentials(Setting.AWS_ACCESS_KEY.getValue(),
				Setting.AWS_SECRET_KEY.getValue());
		handler.s3Client = new AmazonS3Client(cred);
		handler.s3Client.setRegion(Region.getRegion(Regions.fromName(Setting.AWS_REGION.getValue())));
		return handler;
	}

	public List<String> listFiles(Bucket bucket) {
		ObjectListing response = s3Client.listObjects(bucket.getName(), bucket.getPath());
		List<String> files = new ArrayList<String>();
		response.getObjectSummaries().forEach(summary -> {
			String[] arr = summary.getKey().split(SEPARATOR);
			if (arr.length > 1) {
				String fileName = summary.getKey().split(SEPARATOR)[1];
				files.add(fileName);
			}
		});
		return files;
	}

	public void copyFile(Bucket srcBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileCopyLogEntry(srcBucket, targetBucket, fileName));
		if (!simulation) {
			logger.trace("Not simulation , so moving files from " + srcBucket + " to " + targetBucket);
			s3Client.copyObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName, targetBucket.getName(),
					targetBucket.getPath() + SEPARATOR + fileName);
		} else {
			logger.trace("Simulation mode");
			logger.trace("Moving files from " + srcBucket + " to " + targetBucket);
		}
	}

	public void moveFile(Bucket srcBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileMoveLogEntry(srcBucket, targetBucket, fileName));
		if (!simulation) {
			logger.trace("Not simulation , so moving files from " + srcBucket.getName() + SEPARATOR + srcBucket.getPath()
					+ " to " + targetBucket);
			CopyObjectResult result = s3Client.copyObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName,
					targetBucket.getName(), targetBucket.getPath() + SEPARATOR + fileName);
			if (result != null) {
				s3Client.deleteObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName);
			}
		} else {
			logger.trace("Simulation mode");
			logger.trace("Moving files from " + srcBucket.getPath() + " to " + targetBucket.getName());
		}
	}

	public void deleteFile(Bucket bucket, String fileName, Logbook log) {
		log.addEntry(new FileDeleteLogEntry(bucket, fileName));
		if (!simulation) {
			s3Client.deleteObject(bucket.getName(), bucket.getPath() + SEPARATOR + fileName);
		} else {
			logger.trace("Simulation mode");
			logger.trace("Cleaning up file :" + bucket.getName() + SEPARATOR + bucket.getPath() + SEPARATOR + fileName);
		}
	}

}
