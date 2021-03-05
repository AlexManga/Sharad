package fr.ama.sharadback.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "local-storage")
public class LocalStorageConfiguration {

	private String rootPath;

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public Path getPathFor(StorageDomain storageDomain) {
		return Paths.get(rootPath, toString(storageDomain));
	}

	private static String toString(StorageDomain storageDomain) {
		switch (storageDomain) {
		case SHARAD_NOTES:
			return "sharad-notes";
		case SHARAD_TAGS:
			return "sharad-tags";
		default:
			throw new IllegalArgumentException("Unexpected value: " + storageDomain);
		}
	}

}
