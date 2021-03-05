package fr.ama.sharadback.model.note;

import com.fasterxml.jackson.annotation.JsonCreator;

import fr.ama.sharadback.model.storage.StorageId;

public class Note {
	private StorageId noteId;
	private NoteContent content;

	@JsonCreator
	public Note(StorageId id, NoteContent content) {
		this.noteId = id;
		this.content = content;
	}

	public StorageId getNoteId() {
		return noteId;
	}

	public NoteContent getContent() {
		return content;
	}
}
