package fr.ama.sharadback.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;
import fr.ama.sharadback.service.TagService;
import fr.ama.sharadback.service.TagService.TagError;

@RestController
@RequestMapping("/tag")
public class TagController {

	private TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@PostMapping
	public ResponseEntity<StorageId> postTag(@RequestBody TagContent tagContent) {
		return tagService.createTag(tagContent)
				.map(ResponseEntity::ok)
				.onError(TagController::handleDeleteError);
	}

	@GetMapping
	public @ResponseBody List<Tag> getTag() {
		return tagService.getAllTags();
	}

	@DeleteMapping("/{id}/{taggedElements}")
	public ResponseEntity<StorageId> deleteTag(
			@PathVariable("id") String tagId,
			@PathVariable("taggedElements") List<String> taggedElements) {

		return tagService.deleteTags(tagId, taggedElements)
				.map(optionalId -> ok().body(optionalId.orElse(null)))
				.onError(TagController::handleDeleteError);
	}

	private static <T> ResponseEntity<T> handleDeleteError(TagError tagError) {
		switch (tagError.getType()) {
		case DELETE_NO_ELEMENT_TAGGED: {
			return notFound().build();
		}
		case STORAGE_ERROR: {
			return status(INTERNAL_SERVER_ERROR).build();
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + tagError.getType());
		}
	}

}
