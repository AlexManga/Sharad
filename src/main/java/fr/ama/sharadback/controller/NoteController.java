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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.note.Note;
import fr.ama.sharadback.model.note.NoteContent;
import fr.ama.sharadback.model.note.UpdateNote;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.service.NoteService;
import fr.ama.sharadback.service.StorageError;

@RestController
@RequestMapping("/note")
public class NoteController {

	private NoteService noteService;

	public NoteController(NoteService noteService) {
		this.noteService = noteService;
	}

	@PostMapping
	public @ResponseBody ResponseEntity<StorageId> postNote(@RequestBody NoteContent noteContent) {
		return noteService.createNote(noteContent)
				.map(ResponseEntity::ok)
				.onError(NoteController::handleStorageError);
	}

	@GetMapping
	public @ResponseBody List<Note> getNote() {
		return noteService.getAllNotes();
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<Object> deleteNote(@PathVariable("id") String noteId) {
		return noteService.deleteNote(noteId)
				.map(NoteController::handleStorageError)
				.orElse(ok().build());
	}

	@PutMapping
	public ResponseEntity<StorageId> putNote(@RequestBody UpdateNote updateNote) {
		return noteService.modifyNote(updateNote.getPreviousNoteId(), updateNote.getNewContent())
				.map(noteId -> ok().body(noteId))
				.onError(NoteController::handleStorageError);
	}

	private static <T> ResponseEntity<T> handleStorageError(StorageError error) {
		if (error.getType() == StorageError.Type.FILE_DOES_NOT_EXIST) {
			return notFound().build();
		} else {
			return status(INTERNAL_SERVER_ERROR).build();
		}
	}
}
