package fr.ama.sharadback.model;

public class Note {
	public String content;

//	@JsonCreator
	public Note(String content) {
		this.content = content;
	}
}
