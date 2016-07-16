package de.blacklabelops.backuprotator.rotator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRotator {

	Map<LocalDateTime, String> dataMap = new HashMap<LocalDateTime, String>();

	List<LocalDateTime> dates = new ArrayList<LocalDateTime>();

	public AbstractRotator() {
		super();
	}

}