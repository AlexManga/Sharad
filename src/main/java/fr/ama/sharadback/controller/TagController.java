package fr.ama.sharadback.controller;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.ama.sharadback.model.tag.Tag;

@RestController
@RequestMapping("/tag")
public class TagController {

	@PostMapping
	public ResponseEntity<Void> postTag(@RequestBody Tag tag) {
		return ok().build();
	}

}
