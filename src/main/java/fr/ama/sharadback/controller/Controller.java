package fr.ama.sharadback.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.Note;
import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;
import fr.ama.sharadback.service.NoteService;
import fr.ama.sharadback.service.StorageError;

@RestController
@RequestMapping("/note")
public class Controller {

	@Autowired
	private NoteService noteService;

	@PostMapping
	public @ResponseBody NoteId postNote(@RequestBody NoteContent noteContent) {
		return noteService.createNote(noteContent);
	}

	@GetMapping
	public @ResponseBody List<Note> getNote() {
		return noteService.getAllNotes();
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Object> deleteNote(@PathVariable("id") String noteId) {
		return noteService.deleteNote(noteId)
				.map(this::handleStorageErrorOnDelete)
				.orElse(ok().build());
	}

	private ResponseEntity<Object> handleStorageErrorOnDelete(StorageError error) {
		if (error.getType() == StorageError.Type.FILE_DOES_NOT_EXIST) {
			return notFound().build();
		} else {
			return status(INTERNAL_SERVER_ERROR).build();
		}
	}
}
