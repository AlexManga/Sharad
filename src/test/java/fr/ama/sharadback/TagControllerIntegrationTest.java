package fr.ama.sharadback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.delete;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;
import fr.ama.sharadback.service.LocalStorageConfiguration;
import fr.ama.sharadback.service.TagService;

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
		File file = localStorageConfiguration.getPathFor(TagService.STORAGE_DOMAIN).toFile();
		if (file.exists()) {
			delete(file);
		}
	}

	@Test
	void success_on_post_tag() throws Exception {
		String tagStr = objectMapper.writeValueAsString(new TagContent("arbitrary tag", List.of("exampleId")));
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

	@Test
	void posting_then_getting_should_give_back_the_tag() throws Exception {
		TagContent arbitraryTagContent = new TagContent("arbitrary tag",
				List.of("linkedElem1", "linkedElem2", "linkedElem3"));

		String postResponseBody = mockMvc.perform(post("/tag").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(arbitraryTagContent)))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		StorageId storedTagId = objectMapper.readerFor(StorageId.class).readValue(postResponseBody);

		String getResponseBody = mockMvc.perform(get("/tag"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();
		Tag[] retrievedTags = objectMapper.readerFor(Tag[].class).readValue(getResponseBody);

		assertThat(retrievedTags).hasSize(1);
		assertThat(retrievedTags[0])
				.usingRecursiveComparison()
				.isEqualTo(new Tag(storedTagId, arbitraryTagContent));
	}

	@Test
	void deleting_a_non_existing_tag_should_give_back_404() throws Exception {
		mockMvc.perform(delete("/tag/arbitraryNoteId")).andExpect(status().isNotFound());
	}

	@Test
	void posting_then_deleting_should_give_back_no_tag() throws Exception {
		TagContent arbitraryTagContent = new TagContent("arbitrary tag", List.of("elemId1", "elemId2"));

		String postResponseBody = mockMvc.perform(post("/tag").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(arbitraryTagContent)))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();

		StorageId tagId = objectMapper.readValue(postResponseBody, StorageId.class);

		String getResponseBody = mockMvc.perform(get("/tag"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		Tag[] retrievedTags = objectMapper.readerFor(Tag[].class).readValue(getResponseBody);
		assertThat(retrievedTags).hasSize(1);
		assertThat(retrievedTags[0])
				.usingRecursiveComparison()
				.isEqualTo(new Tag(tagId, arbitraryTagContent));

		mockMvc.perform(MockMvcRequestBuilders.delete("/tag/" + tagId.getId() + "/elemId1,elemId2"))
				.andExpect(status().is2xxSuccessful());

		mockMvc.perform(get("/tag"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string("[]"));
	}

}
