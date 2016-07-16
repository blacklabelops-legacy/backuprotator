package de.blacklabelops.backuprotator;

import de.blacklabelops.backuprotator.util.Setting;

public class Main {

	public static void main(String[] args) {
		Setting.setFromDefaults();
		Setting.setFromPropertyFile();
		Setting.setFromEnvironmentVariables();
		Setting.validateConfiguration();
		BackupRotator.getInstance().startBackupRotation();
	}

}
