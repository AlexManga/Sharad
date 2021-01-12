package fr.ama.sharadback.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.Note;

@RestController
public class Controller {

	@PostMapping("/note")
	public String postNote(@RequestBody Note note) {
		return "";
    }
}
