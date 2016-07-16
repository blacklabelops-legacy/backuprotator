package de.blacklabelops.backuprotator.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static LocalDateTime systemTime = LocalDateTime.now();

	public static String getDateFromFileName(String fileName) {
		Pattern p = null;
		String pattern = System.getProperty(Setting.DATE_PATTERN.getKey());
		if (pattern != null && pattern.trim().length() > 0) {
			p = Pattern.compile(pattern);
		} else {
			p = Pattern.compile(Setting.DATE_PATTERN.getDefaultValue());
		}
		Matcher m = p.matcher(fileName);
		while (m.find()) {
			return m.group();
		}
		return null;
	}

	public static LocalDateTime parseDate(String dateString) {
		String format = Setting.DATE_FORMAT.getValue();
		LocalDateTime time = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));
		return time;
	}

	public static boolean isDateInCurrentWeek(LocalDateTime time) {
		LocalDateTime localDateTime = systemTime;
		int week = localDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
		int year = localDateTime.get(ChronoField.YEAR);
		int targetWeek = time.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
		int targetYear = time.get(ChronoField.YEAR);
		return week == targetWeek && year == targetYear;
	}

	public static boolean isDateInCurrentYear(LocalDateTime time) {
		LocalDateTime localDateTime = systemTime;
		int year = localDateTime.get(ChronoField.YEAR);
		int targetYear = time.get(ChronoField.YEAR);
		return year == targetYear;
	}

	public static boolean isDateInCurrentMonth(LocalDateTime time) {
		LocalDateTime localDateTime = systemTime;
		int month = localDateTime.get(ChronoField.MONTH_OF_YEAR);
		int year = localDateTime.get(ChronoField.YEAR);
		int targetMonth = time.get(ChronoField.MONTH_OF_YEAR);
		int targetYear = time.get(ChronoField.YEAR);
		return month == targetMonth && year == targetYear;
	}

	public static List<LocalDateTime> getLatestDatesInEachWeek(List<LocalDateTime> allDates) {
		List<LocalDateTime> requiredDates = new ArrayList<LocalDateTime>();
		Map<Integer, List<LocalDateTime>> weekMap = new HashMap<Integer, List<LocalDateTime>>();
		allDates.forEach(d -> {
			sameDate(weekMap, d, IsoFields.WEEK_OF_WEEK_BASED_YEAR);
		});
		sortDateMap(requiredDates, weekMap);
		Collections.sort(requiredDates);
		return requiredDates;
	}

	private static void sortDateMap(List<LocalDateTime> requiredDates, Map<Integer, List<LocalDateTime>> dateMap) {
		dateMap.forEach((timeKey, dates) -> {
			LocalDateTime latestDate = dates.stream().sorted().reduce((dateA, dateB) -> dateB).orElse(null);
			if (latestDate != null) {
				requiredDates.add(latestDate);
			}
		});
	}

	public static List<LocalDateTime> getLatestDatesInEachMonth(List<LocalDateTime> allDates) {
		List<LocalDateTime> requiredDates = new ArrayList<LocalDateTime>();
		Map<Integer, List<LocalDateTime>> weekMap = new HashMap<Integer, List<LocalDateTime>>();
		allDates.forEach(d -> {
			sameDate(weekMap, d, ChronoField.MONTH_OF_YEAR);
		});
		sortDateMap(requiredDates, weekMap);
		Collections.sort(requiredDates);
		return requiredDates;
	}

	private static void sameDate(Map<Integer, List<LocalDateTime>> weekMap, LocalDateTime d, TemporalField dateField) {
		int month = d.get(dateField);
		int year = d.get(ChronoField.YEAR);
		int offset = year + month;
		if (weekMap.containsKey(offset)) {
			weekMap.get(offset).add(d);
		} else {
			List<LocalDateTime> ds = new ArrayList<LocalDateTime>();
			ds.add(d);
			weekMap.put(offset, ds);
		}
	}

	public static LocalDateTime parseFilename(String fileName) {
		LocalDateTime time = null;
		String fileDate = Utils.getDateFromFileName(fileName);
		if (fileDate != null) {
			time = Utils.parseDate(fileDate);
		}
		return time;
	}

}
