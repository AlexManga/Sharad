package fr.ama.sharadback;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;

@SpringBootTest
@AutoConfigureMockMvc
class SharadBackApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void success_on_post_note() throws Exception {
		String noteStr = objectMapper.writeValueAsString(new NoteContent(""));
		mockMvc.perform(post("/note")
				.contentType(APPLICATION_JSON)
				.content(noteStr))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	void success_on_get_request() throws Exception {
		mockMvc.perform(get("/notes")).andExpect(status().is2xxSuccessful());
	}

	@Test
	void posting_then_getting_should_give_back_the_note() throws Exception {
		String arbitraryNoteContent = "test content of note";

		String postResponseBody = mockMvc.perform(post("/note").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new NoteContent(arbitraryNoteContent))))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();

		NoteId noteId = objectMapper.readValue(postResponseBody, NoteId.class);

		mockMvc.perform(get("/notes"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(
						jsonPath(String.format("$.[?(@.id=='%s')].content", noteId.getId()),
								allOf(Matchers.<String>iterableWithSize(1),
										contains(arbitraryNoteContent))));
	}

}
