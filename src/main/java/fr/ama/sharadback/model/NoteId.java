package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NoteId {

	private String id;
	private String version;

	@JsonCreator
	public NoteId(String id, String version) {
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
