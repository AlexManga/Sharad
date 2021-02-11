package fr.ama.sharadback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.Note;
import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;
import fr.ama.sharadback.service.NoteService;

@RestController
public class Controller {

	@Autowired
	private NoteService noteService;

	@PostMapping("/note")
	public @ResponseBody NoteId postNote(@RequestBody NoteContent noteContent) {
		return noteService.createNote(noteContent);
	}

	@GetMapping("/notes")
	public @ResponseBody List<Note> getNote() {
		return noteService.getAllNotes();
	}

	@DeleteMapping(path = "/note/{id}")
	public void deleteNote(@PathVariable("id") String noteId) {

	}
}
