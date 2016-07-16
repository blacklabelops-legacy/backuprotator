package de.blacklabelops.backuprotator.rotator;

import java.time.LocalDateTime;
import java.util.List;

import de.blacklabelops.backuprotator.Bucket;
import de.blacklabelops.backuprotator.filehandler.FileHandler;
import de.blacklabelops.backuprotator.logbook.Logbook;
import de.blacklabelops.backuprotator.util.Utils;

public class WeeklyRotator extends AbstractRotator {

	public WeeklyRotator() {
		super();
	}

	public void rotateWeekly(Logbook log, FileHandler handler, Bucket weeklyBucket, Bucket monthlyBucket) {
		inspectFiles(handler, weeklyBucket);
		rotateFiles(log, handler, weeklyBucket, monthlyBucket);
	}

	private void inspectFiles(FileHandler handler, Bucket weeklyBucket) {
		handler.listFiles(weeklyBucket).stream().filter(filename -> Utils.parseFilename(filename) != null)
				.forEach(filename -> {
					LocalDateTime d = Utils.parseFilename(filename);
					boolean isInCurrentMonth = Utils.isDateInCurrentMonth(d);
					if (!isInCurrentMonth) {
						dataMap.put(d, filename);
						dates.add(d);
					}
				});
	}

	private void rotateFiles(Logbook log, FileHandler handler, Bucket weeklyBucket, Bucket monthlyBucket) {
		List<LocalDateTime> requiredDates = Utils.getLatestDatesInEachWeek(dates);
		dataMap.forEach((dx, filename) -> {
			if (requiredDates.contains(dx)) {
				handler.moveFile(weeklyBucket, monthlyBucket, filename, log);
			} else {
				handler.deleteFile(weeklyBucket, filename, log);
			}
		});
	}
}
