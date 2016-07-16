package de.blacklabelops.backuprotator.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testGetDateFromFileName() {
		String fileName = "JenkinsBackupV1-2016-06-15-00-15-01.tar.gz.gpg";
		String date = "2016-06-15-00-15-01";

		String actual = Utils.getDateFromFileName(fileName);
		assertTrue("Date extracted is correct", date.equals(actual));
		fileName = "JenkinsBackupV1-2016-XX-15-00-15-01.tar.gz.gpg";
		date = "2016-06-15-00-15-01";

		actual = Utils.getDateFromFileName(fileName);
		assertTrue("Date shouldn't be extracted", !date.equals(actual));
	}

	@Test
	public void testGetDateFromDifferentFilenames() {
		String fileName = "Mammoth-2016-06-15-00-15-01.gpg";
		String date = "2016-06-15-00-15-01";

		String actual = Utils.getDateFromFileName(fileName);
		assertTrue("Date extracted is correct", date.equals(actual));
		fileName = "JenkinsBackupV1-2016.06.15.00.15-01.tar.gz.gpg";
		date = "2016-06-15-00-15-01";

		actual = Utils.getDateFromFileName(fileName);
		assertTrue("Date shouldn't be extracted", !date.equals(actual));
	}

	@Test
	public void testIsDateInCurrentWeek() {
		LocalDateTime d = LocalDateTime.now();
		assertTrue("Current week logic failed", Utils.isDateInCurrentWeek(d));
		d = LocalDateTime.of(2005, 10, 1, 0, 0);
		assertFalse("Current week logic failed", Utils.isDateInCurrentWeek(d));
	}

	@Test
	public void testIsDateInCurrentMonth() {
		LocalDateTime d = LocalDateTime.now();
		assertTrue("Current month logic failed", Utils.isDateInCurrentMonth(d));
		d = LocalDateTime.of(2005, 10, 1, 0, 0);
		assertFalse("Current month logic failed", Utils.isDateInCurrentMonth(d));
	}

	@Test
	public void testIsDateInCurrentYear() {
		LocalDateTime d = LocalDateTime.now();
		assertTrue("Current year logic failed", Utils.isDateInCurrentYear(d));
		d = LocalDateTime.of(2005, 10, 1, 0, 0);
		assertFalse("Current year logic failed", Utils.isDateInCurrentYear(d));
	}

	@Test
	public void testGetLatestDatesInWeek() {
		Locale.setDefault(Locale.UK);
		List<LocalDateTime> allDates = new ArrayList<LocalDateTime>();
		List<LocalDateTime> latestDates = new ArrayList<LocalDateTime>();
		LocalDateTime cal = null;
		cal = LocalDateTime.of(2016, 7, 4, 0, 0);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 7, 7, 0, 0);
		latestDates.add(cal);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 7, 18, 0, 0);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 7, 19, 0, 0);
		latestDates.add(cal);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 7, 25, 0, 0);
		allDates.add(cal);
		latestDates.add(cal);
		List<LocalDateTime> actual = Utils.getLatestDatesInEachWeek(allDates);
		assertTrue("Latest in week checking logic failed", actual.equals(latestDates));
	}

	@Test
	public void testGetLatestDatesInMonth() {
		List<LocalDateTime> allDates = new ArrayList<LocalDateTime>();
		List<LocalDateTime> latestDates = new ArrayList<LocalDateTime>();
		LocalDateTime cal = LocalDateTime.of(2016, 7, 4, 0, 0);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 7, 7, 0, 0);
		latestDates.add(cal);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 5, 18, 0, 0);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 5, 19, 0, 0);
		latestDates.add(cal);
		allDates.add(cal);
		cal = LocalDateTime.of(2016, 8, 25, 0, 0);
		allDates.add(cal);
		latestDates.add(cal);
		Collections.sort(latestDates);
		List<LocalDateTime> actual = Utils.getLatestDatesInEachMonth(allDates);
		assertTrue("Latest in week checking logic failed", actual.equals(latestDates));
	}

}
