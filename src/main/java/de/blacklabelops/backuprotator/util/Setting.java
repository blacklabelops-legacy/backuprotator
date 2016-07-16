package de.blacklabelops.backuprotator.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Setting {

	DAILY_BUCKET_NAME("BACKUPROTATOR_DAILY_BUCKET_NAME", "backups", false), //
	DAILY_BUCKET_PATH("BACKUPROTATOR_DAILY_BUCKET_PATH", "daily", false), //
	WEEKLY_BUCKET_NAME("BACKUPROTATOR_WEEKLY_BUCKET_NAME", "backups", false), //
	WEEKLY_BUCKET_PATH("BACKUPROTATOR_WEEKLY_BUCKET_PATH", "weekly", false), //
	MONTHLY_BUCKET_NAME("BACKUPROTATOR_MONTHLY_BUCKET_NAME", "backups", false), //
	MONTHLY_BUCKET_PATH("BACKUPROTATOR_MONTHLY_BUCKET_PATH", "monthly", false), //
	YEARLY_BUCKET_NAME("BACKUPROTATOR_YEARLY_BUCKET_NAME", "backups", false), //
	YEARLY_BUCKET_PATH("BACKUPROTATOR_YEARLY_BUCKET_PATH", "yearly", false), //
	DATE_PATTERN("BACKUPROTATOR_DATE_PATTERN", "(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})", false), //
	DATE_FORMAT("BACKUPROTATOR_DATE_FORMAT", "yyyy-MM-dd-HH-mm-ss", false), //
	SIMULATION_MODE("BACKUPROTATOR_SIMULATION_MODE", null, true), //
	AWS_ACCESS_KEY("BACKUPROTATOR_AWS_ACCESS_KEY", null, true), //
	AWS_SECRET_KEY("BACKUPROTATOR_AWS_SECRET_KEY", null, true), //
	AWS_REGION("BACKUPROTATOR_AWS_REGION", null, true);

	private static final String PROPERTY_FILE_NAME = "backuprotator.properties";

	private final static Logger logger = LoggerFactory.getLogger(Setting.class);

	private String value;

	private String key;

	private String defaultValue;

	private boolean required;

	Setting(String pKey, String pDefaultValue, boolean pRequired) {
		this.key = pKey;
		this.defaultValue = pDefaultValue;
		this.required = pRequired;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public static void setFromDefaults() {
		for (Setting currentSetting : Setting.values()) {
			currentSetting.setFromDefault();
		}
	}

	public void setFromDefault() {
		setValue(getDefaultValue());
	}

	public boolean isRequired() {
		return required;
	}

	public static void setFromEnvironmentVariables() {
		for (Setting currentSetting : Setting.values()) {
			currentSetting.setFromEnvironmentVariable();
		}
	}

	private void setFromEnvironmentVariable() {
		String environmentVariableValue = System.getenv(getKey());
		if (environmentVariableValue != null && !environmentVariableValue.isEmpty()) {
			setValue(environmentVariableValue);
		}
	}

	public static void setFromPropertyFile() {
		InputStream is = Setting.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
		Properties properties = new Properties();
		if (is != null) {
			readConfigfile(is, properties);
		}
	}

	private static void readConfigfile(InputStream is, Properties properties) {
		logger.debug("Going to read parameters from the property file.");
		try {
			properties.load(is);
			for (Setting currentSetting : Setting.values()) {
				String property = properties.getProperty(currentSetting.getKey());
				if (property != null && !property.isEmpty()) {
					currentSetting.setValue(property);
				}
			}
		} catch (IOException e) {
			logger.error("Error occured while loading property file.Going to read from enviornment", e);
		}
	}

	public static void validateConfiguration() {
		for (Setting currentSetting : Setting.values()) {
			if (currentSetting.isRequired() && Setting.isEmpty(currentSetting.getValue())) {
				RuntimeException configurationError = new RuntimeException(currentSetting.getKey() + " not set!");
				logger.error("Configuration Key Error", configurationError);
				throw configurationError;
			}
		}
	}

	public static boolean isEmpty(String value) {
		boolean empty = true;
		if (value != null && !value.isEmpty()) {
			empty = false;
		}
		return empty;
	}

}
