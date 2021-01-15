package fr.ama.sharadback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;
import fr.ama.sharadback.service.NoteService;

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
	public String getNote() {
		return "";
	}
}
