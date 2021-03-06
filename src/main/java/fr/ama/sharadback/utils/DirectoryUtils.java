package fr.ama.sharadback.utils;

import java.io.File;
import java.util.UUID;

public class DirectoryUtils {

	public static boolean createDirOrCheckAccess(File dirToCreateOrCheck) {
		if (!dirToCreateOrCheck.exists()) {
			dirToCreateOrCheck.mkdirs();
			return true;
		} else
			return checkStorageDirectory(dirToCreateOrCheck);
	}

	private static boolean checkStorageDirectory(File dirToCreateOrCheck) {
		if (!dirToCreateOrCheck.isDirectory() || !dirToCreateOrCheck.canRead()
				|| !dirToCreateOrCheck.canWrite()) {
			return false;
		}
		return true;
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

}
