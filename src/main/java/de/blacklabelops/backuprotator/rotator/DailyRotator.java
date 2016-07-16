package de.blacklabelops.backuprotator.rotator;

import java.time.LocalDateTime;
import java.util.List;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.filehandler.FileHandler;
import de.blacklabelops.backuprotator.logbook.Logbook;
import de.blacklabelops.backuprotator.util.Utils;

public class DailyRotator extends AbstractRotator {

	public DailyRotator() {
		super();
	}

	public void rotateDaily(Logbook log, FileHandler handler, Bucket dailyBucket, Bucket weeklyBucket) {
		inspectFiles(handler, dailyBucket);
		rotateFiles(log, handler, dailyBucket, weeklyBucket);
	}

	private void inspectFiles(FileHandler handler, Bucket dailyBucket) {
		handler.listFiles(dailyBucket).forEach(fileName -> {
			LocalDateTime d = Utils.parseFilename(fileName);
			if (d == null) {
				return;
			}
			boolean isInCurrentWeek = Utils.isDateInCurrentWeek(d);
			if (!isInCurrentWeek) {
				dataMap.put(d, fileName);
				dates.add(d);
			}
		});
	}

	private void rotateFiles(Logbook log, FileHandler handler, Bucket dailyBucket, Bucket weeklyBucket) {
		List<LocalDateTime> requiredDates = Utils.getLatestDatesInEachWeek(dates);
		dataMap.forEach((dx, filename) -> {
			if (requiredDates.contains(dx)) {
				handler.moveFile(dailyBucket, weeklyBucket, filename, log);
			} else {
				handler.deleteFile(dailyBucket, filename, log);
			}
		});
	}
}
