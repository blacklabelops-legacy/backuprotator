package de.blacklabelops.backuprotator.rotator;

import java.time.LocalDateTime;
import java.util.List;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.filehandler.FileHandler;
import de.blacklabelops.backuprotator.logbook.Logbook;
import de.blacklabelops.backuprotator.util.Utils;

public class MonthlyRotator extends AbstractRotator {

	public MonthlyRotator() {
		super();
	}

	public void rotateMonthly(Logbook log, FileHandler handler, Bucket monthlyBucket, Bucket yearlyBucket) {
		inspectFiles(handler, monthlyBucket);
		rotateFiles(log, handler, monthlyBucket, yearlyBucket);
	}

	private void inspectFiles(FileHandler handler, Bucket monthlyBucket) {
		handler.listFiles(monthlyBucket).stream().filter(filename -> Utils.parseFilename(filename) != null)
				.forEach(filename -> {
					LocalDateTime d = Utils.parseFilename(filename);
					boolean isInCurrentMonth = Utils.isDateInCurrentYear(d);
					if (!isInCurrentMonth) {
						dataMap.put(d, filename);
						dates.add(d);
					}
				});
	}

	private void rotateFiles(Logbook log, FileHandler handler, Bucket monthlyBucket, Bucket yearlyBucket) {
		List<LocalDateTime> requiredDates = Utils.getLatestDatesInEachMonth(dates);
		dataMap.forEach((dx, filename) -> {
			if (requiredDates.contains(dx)) {
				handler.copyFile(monthlyBucket, yearlyBucket, filename, log);
			} else {
				handler.deleteFile(monthlyBucket, filename, log);
			}
		});
	}

}
