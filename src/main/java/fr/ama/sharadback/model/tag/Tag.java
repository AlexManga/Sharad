package fr.ama.sharadback.model.tag;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Tag {

	private String tag;
	private List<String> taggedElementsIds = List.of();

	@JsonCreator
	public Tag(String tag, List<String> taggedElementsIds) {
		this.tag = tag.strip();
		if (taggedElementsIds != null) {
			this.taggedElementsIds = taggedElementsIds;
		}
	}

	public String getTag() {
		return tag;
	}

	public List<String> getTaggedElementsIds() {
		return taggedElementsIds;
	}

}
