package fr.ama.sharadback.model.note;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NoteContent {
	private String title;
	private String body;

	@JsonCreator
	public NoteContent(String title, String body) {
		this.title = title;
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public String getTitle() {
		return title;
	}
}
