package fr.ama.sharadback.model.tag;

import com.fasterxml.jackson.annotation.JsonCreator;

import fr.ama.sharadback.model.storage.StorageId;

public class Tag {

	private StorageId tagId;
	private TagContent content;

	@JsonCreator
	public Tag(StorageId tagId, TagContent content) {
		this.tagId = tagId;
		this.content = content;
	}

	public StorageId getTagId() {
		return tagId;
	}

	public TagContent getContent() {
		return content;
	}

}
