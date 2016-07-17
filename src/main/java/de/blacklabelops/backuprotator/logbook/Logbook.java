package de.blacklabelops.backuprotator.logbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Logbook {

	private final static Logger logger = LoggerFactory.getLogger(Logbook.class);

	private List<ILogEntry> entries = new ArrayList<>();

	public Logbook() {
	}

	public void addEntry(ILogEntry entry) {
		entries.add(entry);
	}

	public void writeLogbookToLog() {
		entries.forEach(entry - > logger.info(entry.getMessage());
	}

}
