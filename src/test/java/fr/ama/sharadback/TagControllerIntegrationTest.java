package fr.ama.sharadback;

import static org.assertj.core.util.Files.delete;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.service.LocalStorageConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TagControllerIntegrationTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();
	private LocalStorageConfiguration localStorageConfiguration;

	@Autowired
	public TagControllerIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper,
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
	void success_on_post_tag() throws Exception {
		String tagStr = objectMapper.writeValueAsString(new Tag("arbitrary tag", List.of("exampleId")));
		mockMvc.perform(post("/tag")
				.contentType(APPLICATION_JSON)
				.content(tagStr))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	void success_on_get_tag() throws Exception {
		mockMvc.perform(get("/tag"))
				.andExpect(status().is2xxSuccessful());
	}

}
