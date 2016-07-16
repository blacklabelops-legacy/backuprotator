package de.blacklabelops.backuprotator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.blacklabelops.backuprotator.util.Setting;
import de.blacklabelops.backuprotator.util.Utils;

public class BackupRotatorTest {

	private final static Logger logger = LoggerFactory.getLogger(BackupRotatorTest.class);

	String root = System.getProperty("java.io.tmpdir");
	String testFolder = root + Setting.DAILY_BUCKET_NAME.getDefaultValue();
	LocalDateTime testTime = LocalDateTime.of(2015, 12, 20, 0, 0);

	List<String> weeklyFiles = new ArrayList<>();
	List<String> dailyFiles = new ArrayList<>();
	List<String> monthlyFiles = new ArrayList<>();
	List<String> yearlyFiles = new ArrayList<>();
	List<String> testFiles = new ArrayList<>();

	@Before
	public void buildUp() throws IOException {
		Locale.setDefault(Locale.UK);
		Utils.systemTime = testTime;
		buildTestData();
		initialiazeSettings();
	}

	@After
	public void tearDown() throws IOException {
		Utils.systemTime = LocalDateTime.now();
		if ((new File(testFolder)).exists()) {
			deleteTestFolder();
		}
		weeklyFiles.clear();
		dailyFiles.clear();
		monthlyFiles.clear();
	}

	private void deleteTestFolder() throws IOException {
		Path directory = Paths.get(testFolder);
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Test(expected = RuntimeException.class)
	public void testConfigurationError() {
		Setting.validateConfiguration();
	}

	@Test
	public void testRotationWithSimulation() {
		Setting.SIMULATION_MODE.setValue("true");
		BackupRotator rotator = BackupRotator.getInstance();
		rotator.startBackupRotation();
		assertSimulationMode();
	}

	private void assertSimulationMode() {
		for (String filename : testFiles) {
			File file = new File(getTestPath(BucketType.DAILY_BUCKET, filename));
			Assert.assertTrue(file.exists());
			file = new File(getTestPath(BucketType.WEEKLY_BUCKET, filename));
			Assert.assertFalse(file.exists());
			file = new File(getTestPath(BucketType.MONTHLY_BUCKET, filename));
			Assert.assertFalse(file.exists());
			file = new File(getTestPath(BucketType.YEARLY_BUCKET, filename));
			Assert.assertFalse(file.exists());
		}
	}

	private void initialiazeSettings() {
		Setting.setFromDefaults();
	}

	@Test
	public void testRotationWithOutSimulation() {
		Setting.SIMULATION_MODE.setValue("false");
		BackupRotator rotator = BackupRotator.getInstance();
		rotator.startBackupRotation();
		weeklyFiles.forEach(this::assertFileMovedToWeekly);
		monthlyFiles.forEach(this::assertFileMovedToMonthly);
		yearlyFiles.forEach(this::assertFileMovedToYearly);
	}

	private void assertFileMovedToWeekly(String filename) {
		File file = new File(getTestPath(BucketType.DAILY_BUCKET, filename));
		Assert.assertFalse(file.exists());
		file = new File(getTestPath(BucketType.WEEKLY_BUCKET, filename));
		Assert.assertTrue(file.exists());
	}

	private void assertFileMovedToMonthly(String filename) {
		File file = new File(getTestPath(BucketType.DAILY_BUCKET, filename));
		Assert.assertFalse(file.exists());
		file = new File(getTestPath(BucketType.WEEKLY_BUCKET, filename));
		Assert.assertFalse(file.exists());
		file = new File(getTestPath(BucketType.MONTHLY_BUCKET, filename));
		Assert.assertTrue(file.exists());
	}

	private void assertFileMovedToYearly(String filename) {
		File file = new File(getTestPath(BucketType.DAILY_BUCKET, filename));
		Assert.assertFalse(file.exists());
		file = new File(getTestPath(BucketType.WEEKLY_BUCKET, filename));
		Assert.assertFalse(file.exists());
		file = new File(getTestPath(BucketType.MONTHLY_BUCKET, filename));
		Assert.assertTrue(file.exists());
		file = new File(getTestPath(BucketType.YEARLY_BUCKET, filename));
		Assert.assertTrue(file.exists());
	}

	private String getTestPath(BucketType bucket, String filename) {
		String bucketname = null;
		String bucketpath = null;
		switch (bucket) {
		case DAILY_BUCKET: {
			bucketname = Setting.DAILY_BUCKET_NAME.getDefaultValue();
			bucketpath = Setting.DAILY_BUCKET_PATH.getDefaultValue();
			break;
		}
		case WEEKLY_BUCKET: {
			bucketname = Setting.WEEKLY_BUCKET_NAME.getDefaultValue();
			bucketpath = Setting.WEEKLY_BUCKET_PATH.getDefaultValue();
			break;
		}
		case MONTHLY_BUCKET: {
			bucketname = Setting.MONTHLY_BUCKET_NAME.getDefaultValue();
			bucketpath = Setting.MONTHLY_BUCKET_PATH.getDefaultValue();
			break;
		}
		case YEARLY_BUCKET: {
			bucketname = Setting.YEARLY_BUCKET_NAME.getDefaultValue();
			bucketpath = Setting.YEARLY_BUCKET_PATH.getDefaultValue();
			break;
		}
		}
		return root + bucketname + File.separator + bucketpath + File.separator + filename;
	}

	private void buildTestData() {
		testFiles.addAll(createWeeklyTestFiles());
		testFiles.addAll(createYearlyTestfiles());
		testFiles.addAll(createMonthlyTestFiles());
		buildDirectories();
		String dailyPath = root + Setting.DAILY_BUCKET_NAME.getDefaultValue() + File.separator
				+ Setting.DAILY_BUCKET_PATH.getDefaultValue();
		testFiles.forEach(name -> {
			String fileName = dailyPath + File.separator + name;
			try {
				File f = new File(fileName);
				f.createNewFile();
			} catch (IOException e) {
				logger.error("Cannot create testfiles!", e);
			}
		});
	}

	private Collection<? extends String> createWeeklyTestFiles() {
		List<String> testFiles = new ArrayList<>();
		testFiles.add("JenkinsBackupV1-2015-12-07-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-08-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-09-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-10-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-11-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-12-00-15-02.tar.gz.gpg");
		testFiles.add("JenkinsBackupV1-2015-12-13-00-15-02.tar.gz.gpg");
		weeklyFiles.add("JenkinsBackupV1-2015-12-13-00-15-02.tar.gz.gpg");
		return testFiles;
	}

	private Collection<? extends String> createMonthlyTestFiles() {
		List<String> testFiles = new ArrayList<>();
		String monthlyFile = "JenkinsBackupV1-2015-01-07-00-15-02.tar.gz.gpg";
		testFiles.add(monthlyFile);
		monthlyFile = "JenkinsBackupV1-2015-01-08-00-15-02.tar.gz.gpg";
		testFiles.add(monthlyFile);
		monthlyFiles.add(monthlyFile);
		monthlyFile = "JenkinsBackupV1-2015-05-15-00-15-02.tar.gz.gpg";
		testFiles.add(monthlyFile);
		monthlyFiles.add(monthlyFile);
		monthlyFile = "JenkinsBackupV1-2015-12-13-00-15-02.tar.gz.gpg";
		testFiles.add(monthlyFile);
		return testFiles;
	}

	private Collection<? extends String> createYearlyTestfiles() {
		List<String> testFiles = new ArrayList<>();
		testFiles.add("JenkinsBackupV1-2014-05-31-00-15-02.tar.gz.gpg");
		yearlyFiles.addAll(testFiles);
		return testFiles;
	}

	private void buildDirectories() {
		File rootFolder = new File(testFolder);
		rootFolder.mkdirs();
		String subFolder = rootFolder.getAbsolutePath() + File.separator + Setting.DAILY_BUCKET_PATH.getDefaultValue();
		new File(subFolder).mkdir();
		subFolder = rootFolder.getAbsolutePath() + File.separator + Setting.WEEKLY_BUCKET_PATH.getDefaultValue();
		new File(subFolder).mkdir();
		subFolder = rootFolder.getAbsolutePath() + File.separator + Setting.MONTHLY_BUCKET_PATH.getDefaultValue();
		new File(subFolder).mkdir();
		subFolder = rootFolder.getAbsolutePath() + File.separator + Setting.YEARLY_BUCKET_PATH.getDefaultValue();
		new File(subFolder).mkdir();
	}

}
