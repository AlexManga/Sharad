package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Note {
	private String id;
	private String content;

	@JsonCreator
	public Note(String id, String content) {
		this.id = id;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public String getContent() {
		return content;
	}
}
