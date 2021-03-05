package fr.ama.sharadback.utils;

import java.io.File;
import java.util.UUID;

import fr.ama.sharadback.controller.FatalException;

public class DirectoryUtils {

	public static void createDirOrCheckAccess(File dirToCreateOrCheck) {
		if (!dirToCreateOrCheck.exists()) {
			dirToCreateOrCheck.mkdirs();
		} else if (!dirToCreateOrCheck.isDirectory() || !dirToCreateOrCheck.canRead()
				|| !dirToCreateOrCheck.canWrite()) {
			throw new FatalException(
					String.format("unable to access storageDir %s", dirToCreateOrCheck.getAbsolutePath()));
		}
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

}
