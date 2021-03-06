package fr.ama.sharadback.model.note;

import com.fasterxml.jackson.annotation.JsonCreator;
import fr.ama.sharadback.model.storage.StorageId;

public class UpdateNote {

	private StorageId previousNoteId;
	private NoteContent newContent;

	@JsonCreator
	public UpdateNote(StorageId previousNoteId, NoteContent newContent) {
		this.previousNoteId = previousNoteId;
		this.newContent = newContent;
	}

	public StorageId getPreviousNoteId() {
		return previousNoteId;
	}

	public NoteContent getNewContent() {
		return newContent;
	}

}
