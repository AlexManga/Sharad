package fr.ama.sharadback.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;
import fr.ama.sharadback.service.TagService;

@RestController
@RequestMapping("/tag")
public class TagController {

	private TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@PostMapping
	public @ResponseBody StorageId postTag(@RequestBody TagContent tagContent) {
		return tagService.createTag(tagContent);
	}

	@GetMapping
	public @ResponseBody List<Tag> getTag() {
		return tagService.getAllTags();
	}

	@DeleteMapping("/{id}/{taggedElements}")
	public @ResponseBody StorageId deleteTag(String tagId, List<String> taggedElements) {
		return tagService.deleteTags(tagId, taggedElements);
	}

}
