package fr.ama.sharadback.model;

public class UpdateNote {

	private NoteId previousNoteId;
	private String newContent;

	public UpdateNote(NoteId previousNoteId, String newContent) {
		this.previousNoteId = previousNoteId;
		this.newContent = newContent;
	}

	public NoteId getPreviousNoteId() {
		return previousNoteId;
	}

	public String getNewContent() {
		return newContent;
	}

}
