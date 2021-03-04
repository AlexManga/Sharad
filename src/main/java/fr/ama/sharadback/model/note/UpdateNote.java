package fr.ama.sharadback.model.note;

public class UpdateNote {

	private NoteId previousNoteId;
	private NoteContent newContent;

	public UpdateNote(NoteId previousNoteId, NoteContent newContent) {
		this.previousNoteId = previousNoteId;
		this.newContent = newContent;
	}

	public NoteId getPreviousNoteId() {
		return previousNoteId;
	}

	public NoteContent getNewContent() {
		return newContent;
	}

}
