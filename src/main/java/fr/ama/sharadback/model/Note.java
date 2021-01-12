package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Note {
	public String content;

	@JsonCreator
	public Note(String content) {
		this.content = content;
	}
}
