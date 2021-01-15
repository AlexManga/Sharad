package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NoteContent {
	private String content;

	@JsonCreator
	public NoteContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
}
