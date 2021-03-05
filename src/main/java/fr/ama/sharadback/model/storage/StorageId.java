package fr.ama.sharadback.model.storage;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StorageId {

	private String id;
	private String version;

	@JsonCreator
	public StorageId(String id, String version) {
		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

}
