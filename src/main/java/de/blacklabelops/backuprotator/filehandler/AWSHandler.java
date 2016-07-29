package de.blacklabelops.backuprotator.filehandler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.StorageClass;

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
	
	private final static long FIVE_GB = 5 * (long)Math.pow(2.0, 30.0);

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
		List<String> files = new ArrayList<>();
		response.getObjectSummaries().forEach(summary -> {
			logger.trace("Found AWS file {}", summary.getKey());
			String[] arr = summary.getKey().split(SEPARATOR);
			if (arr.length > 1) {
				String fileName = summary.getKey().split(SEPARATOR)[arr.length-1];
				logger.trace("Adding filename {}", fileName);
				files.add(fileName);
			}
		});
		return files;
	}

	public void copyFile(Bucket srcBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileCopyLogEntry(srcBucket, targetBucket, fileName));
		ObjectMetadata metadata = getMetaData(srcBucket, fileName);
		if (!simulation && isAccessibleStorageClass(metadata)) {
			logger.trace("Not simulation , so moving files from {} to {}", srcBucket, targetBucket);
			long size = getFileSize(metadata);
			if (size > FIVE_GB) {
				logger.trace("Big file multi attachement copy chosen, file size {}", size);
				copyMultipartFile(srcBucket, targetBucket, fileName, size);
			} else {
				logger.trace("Small file copy chosen, file size {}", size);
				s3Client.copyObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName, targetBucket.getName(),
					targetBucket.getPath() + SEPARATOR + fileName);
			}
		} else {
			logger.trace("Simulation mode or wrong storage class, moving files from {} to {}", srcBucket, targetBucket);
		}
	}

	public void moveFile(Bucket srcBucket, Bucket targetBucket, String fileName, Logbook log) {
		log.addEntry(new FileMoveLogEntry(srcBucket, targetBucket, fileName));
		ObjectMetadata metadata = getMetaData(srcBucket, fileName);
		if (!simulation && isAccessibleStorageClass(metadata)) {
			logger.trace("Not simulation , so moving files from {}/{} to {}", srcBucket.getName(), srcBucket.getPath(), targetBucket);
			long size = getFileSize(metadata);
			CopyObjectResult result = null;
			if (size > FIVE_GB) {
				logger.trace("Big file multi attachement copy chosen, file size {}", size);
				CompleteMultipartUploadResult completeUploadResponse = copyMultipartFile(srcBucket, targetBucket,
						fileName, size);
			    if (completeUploadResponse != null) {
			    	s3Client.deleteObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName);
			    }
			    
			} else {
				logger.trace("Small file movement chosen, file size {}", size);
				result = s3Client.copyObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName,
						targetBucket.getName(), targetBucket.getPath() + SEPARATOR + fileName);
				if (result != null) {
					s3Client.deleteObject(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName);
				}
			}
		} else {
			logger.trace("Simulation mode or wrong storage class, moving files from {} to {}", srcBucket.getPath(), targetBucket.getName());
		}
	}

	private CompleteMultipartUploadResult copyMultipartFile(Bucket srcBucket, Bucket targetBucket, String fileName,
			long size) {
		// Create lists to hold copy responses
		List<CopyPartResult> copyResponses =
		        new ArrayList<CopyPartResult>();

		// Step 2: Initialize
		InitiateMultipartUploadRequest initiateRequest = 
		      	new InitiateMultipartUploadRequest(targetBucket.getName(), targetBucket.getPath() + SEPARATOR + fileName);
		        
		InitiateMultipartUploadResult initResult = 
		       	s3Client.initiateMultipartUpload(initiateRequest);
		
		 // Step 4. Copy parts.
		long partSize = 5 * (long)Math.pow(2.0, 20.0); // 5 MB
		long bytePosition = 0;
		for (int i = 1; bytePosition < size; i++)
		{
		    // Step 5. Save copy response.
			CopyPartRequest copyRequest = new CopyPartRequest()
		       .withDestinationBucketName(targetBucket.getName())
		       .withDestinationKey(targetBucket.getPath() + SEPARATOR + fileName)
		       .withSourceBucketName(srcBucket.getName())
		       .withSourceKey(srcBucket.getPath() + SEPARATOR + fileName)
		       .withUploadId(initResult.getUploadId())
		       .withFirstByte(bytePosition)
		       .withLastByte(bytePosition + partSize -1 >= size ? size - 1 : bytePosition + partSize - 1) 
		       .withPartNumber(i);

		    copyResponses.add(s3Client.copyPart(copyRequest));
		    bytePosition += partSize;
		}
		CompleteMultipartUploadRequest completeRequest = new 
		    	CompleteMultipartUploadRequest(
		    			targetBucket.getName(),
		    			targetBucket.getPath() + SEPARATOR + fileName,
		    			initResult.getUploadId(),
		    			GetETags(copyResponses));
		// Step 7. Complete copy operation.
		CompleteMultipartUploadResult completeUploadResponse =
		    s3Client.completeMultipartUpload(completeRequest);
		return completeUploadResponse;
	}
	
	private ObjectMetadata getMetaData(Bucket srcBucket, String fileName) {
		GetObjectMetadataRequest metadataRequest = 
		    	new GetObjectMetadataRequest(srcBucket.getName(), srcBucket.getPath() + SEPARATOR + fileName);
		ObjectMetadata metadataResult = s3Client.getObjectMetadata(metadataRequest);
		return metadataResult;
	}
	
	private long getFileSize(ObjectMetadata metadata) {
		return metadata.getContentLength();
	}
	
	private boolean isAccessibleStorageClass(ObjectMetadata metadata) {
		boolean accessible = false;
		String storageClass = metadata.getStorageClass();
		if (storageClass == null || StorageClass.Standard.equals(StorageClass.fromValue(storageClass))) {
			accessible = true;
		}
		return accessible;
	}

	public void deleteFile(Bucket bucket, String fileName, Logbook log) {
		log.addEntry(new FileDeleteLogEntry(bucket, fileName));
		ObjectMetadata metadata = getMetaData(bucket, fileName);
		if (!simulation && isAccessibleStorageClass(metadata)) {
			s3Client.deleteObject(bucket.getName(), bucket.getPath() + SEPARATOR + fileName);
		} else {
			logger.trace("Simulation mode or wrong storage class, cleaning up file : {}/{}/{}", bucket.getName(), bucket.getPath(), fileName);
		}
	}
	
	// Helper function that constructs ETags.
    static List<PartETag> GetETags(List<CopyPartResult> responses)
    {
        List<PartETag> etags = new ArrayList<PartETag>();
        for (CopyPartResult response : responses)
        {
            etags.add(new PartETag(response.getPartNumber(), response.getETag()));
        }
        return etags;
    }   

}
