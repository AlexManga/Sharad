package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NoteId {
	
	private String id;

	@JsonCreator
	public NoteId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
