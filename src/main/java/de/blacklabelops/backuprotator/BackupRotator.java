package de.blacklabelops.backuprotator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.blacklabelops.backuprotator.filehandler.AWSHandler;
import de.blacklabelops.backuprotator.filehandler.FileHandler;
import de.blacklabelops.backuprotator.filehandler.LocalFileSystemHandler;
import de.blacklabelops.backuprotator.logbook.Logbook;
import de.blacklabelops.backuprotator.rotator.DailyRotator;
import de.blacklabelops.backuprotator.rotator.MonthlyRotator;
import de.blacklabelops.backuprotator.rotator.WeeklyRotator;
import de.blacklabelops.backuprotator.util.Setting;

public class BackupRotator {

	private final static Logger logger = LoggerFactory.getLogger(BackupRotator.class);

	private Bucket dailyBucket;

	private Bucket weeklyBucket;

	private Bucket monthlyBucket;

	private Bucket yearlyBucket;

	private boolean isSimulation;

	private BackupRotator() {
		super();
	}

	public static BackupRotator getInstance() {
		BackupRotator rotator = new BackupRotator();
		rotator.dailyBucket = new Bucket(Setting.DAILY_BUCKET_NAME.getValue(), Setting.DAILY_BUCKET_PATH.getValue());
		rotator.weeklyBucket = new Bucket(Setting.WEEKLY_BUCKET_NAME.getValue(), Setting.WEEKLY_BUCKET_PATH.getValue());
		rotator.monthlyBucket = new Bucket(Setting.MONTHLY_BUCKET_NAME.getValue(),
				Setting.MONTHLY_BUCKET_PATH.getValue());
		rotator.yearlyBucket = new Bucket(Setting.YEARLY_BUCKET_NAME.getValue(), Setting.YEARLY_BUCKET_PATH.getValue());
		rotator.isSimulation = Boolean.parseBoolean(Setting.SIMULATION_MODE.getValue());
		return rotator;
	}

	public void startBackupRotation() {
		if (isSimulation) {
			logger.info("SIMULATION MODE ON: Nothing will be written!");
		}
		FileHandler handler = createHandler(isSimulation);
		Logbook log = new Logbook();
		dailyRotation(handler, log);
		weeklyRotation(handler, log);
		monthlyTotation(handler, log);
		log.writeLogbookToLog();
	}

	private FileHandler createHandler(boolean isSimulation) {
		FileHandler handler = null;
		if (!Setting.isEmpty(Setting.AWS_ACCESS_KEY.getValue())) {
			handler = AWSHandler.getInstance(isSimulation);
		} else {
			handler = LocalFileSystemHandler.getInstance(isSimulation);
		}
		return handler;
	}

	private void monthlyTotation(FileHandler handler, Logbook log) {
		logger.info("START MONTHLY ROTATION");
		new MonthlyRotator().rotateMonthly(log, handler, monthlyBucket, yearlyBucket);
		logger.info("END MONTHLY ROTATION");
	}

	private void weeklyRotation(FileHandler handler, Logbook log) {
		logger.info("START WEEKLY ROTATION");
		new WeeklyRotator().rotateWeekly(log, handler, weeklyBucket, monthlyBucket);
		logger.info("END WEEKLY ROTATION");
	}

	private void dailyRotation(FileHandler handler, Logbook log) {
		logger.info("START DAILY ROTATION");
		new DailyRotator().rotateDaily(log, handler, dailyBucket, weeklyBucket);
		logger.info("END DAILY ROTATION");
	}

}
