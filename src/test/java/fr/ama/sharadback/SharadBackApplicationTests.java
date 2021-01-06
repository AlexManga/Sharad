package fr.ama.sharadback;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.Note;

@SpringBootTest
@AutoConfigureMockMvc
class SharadBackApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void success_on_post_note() throws Exception {
		String noteStr = objectMapper.writeValueAsString(new Note(""));
		mockMvc.perform(post("/note")
				.contentType(APPLICATION_JSON)
				.content(noteStr))
				.andExpect(status().is2xxSuccessful());
	}

}
