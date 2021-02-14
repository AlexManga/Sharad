package fr.ama.sharadback.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Note {
	private NoteId noteId;
	private String content;

	@JsonCreator
	public Note(NoteId id, String content) {
		this.noteId = id;
		this.content = content;
	}

	public NoteId getNoteId() {
		return noteId;
	}

	public String getContent() {
		return content;
	}
}
