package fr.ama.sharadback;

import static org.assertj.core.util.Files.delete;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;
import fr.ama.sharadback.service.LocalStorageConfiguration;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class SharadBackApplicationTests {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();
	private LocalStorageConfiguration localStorageConfiguration;

	@Autowired
	public SharadBackApplicationTests(MockMvc mockMvc, ObjectMapper objectMapper,
			LocalStorageConfiguration localStorageConfiguration) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	@BeforeEach
	void before() {
		File file = new File(localStorageConfiguration.getRootPath());
		if (file.exists()) {
			delete(file);
		}
	}

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
		mockMvc.perform(get("/note")).andExpect(status().is2xxSuccessful());
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

		mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(
						jsonPath(String.format("$.[?(@.id=='%s')].content", noteId.getId()),
								allOf(Matchers.<String>iterableWithSize(1),
										contains(arbitraryNoteContent))));
	}

	@Test
	void success_on_delete_request() throws Exception {
		mockMvc.perform(delete("/note/arbitraryNoteId")).andExpect(status().is2xxSuccessful());
	}

	@Test
	void posting_then_deleting_should_give_back_no_note() throws Exception {
		String arbitraryNoteContent = "test content of note to be deleted";

		String postResponseBody = mockMvc.perform(post("/note").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new NoteContent(arbitraryNoteContent))))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();

		NoteId noteId = objectMapper.readValue(postResponseBody, NoteId.class);

		mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(
						jsonPath(String.format("$.[?(@.id=='%s')].content", noteId.getId()),
								allOf(Matchers.<String>iterableWithSize(1),
										contains(arbitraryNoteContent))));

		mockMvc.perform(MockMvcRequestBuilders.delete("/note/" + noteId.getId()))
				.andExpect(status().is2xxSuccessful());

		mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string("[]"));
	}
}
