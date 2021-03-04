package fr.ama.sharadback.model.note;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Note {
	private NoteId noteId;
	private NoteContent content;

	@JsonCreator
	public Note(NoteId id, NoteContent content) {
		this.noteId = id;
		this.content = content;
	}

	public NoteId getNoteId() {
		return noteId;
	}

	public NoteContent getContent() {
		return content;
	}
}
