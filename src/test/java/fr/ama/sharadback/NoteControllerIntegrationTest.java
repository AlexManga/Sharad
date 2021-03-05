package fr.ama.sharadback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.delete;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.note.Note;
import fr.ama.sharadback.model.note.NoteContent;
import fr.ama.sharadback.model.note.UpdateNote;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.service.LocalStorageConfiguration;
import fr.ama.sharadback.service.NoteService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class NoteControllerIntegrationTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();
	private LocalStorageConfiguration localStorageConfiguration;

	@Autowired
	public NoteControllerIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper,
			LocalStorageConfiguration localStorageConfiguration) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	@BeforeEach
	void before() {
		File file = localStorageConfiguration.getPathFor(NoteService.STORAGE_DOMAIN).toFile();
		if (file.exists()) {
			delete(file);
		}
	}

	@Test
	void success_on_post_note() throws Exception {
		String noteStr = objectMapper.writeValueAsString(new NoteContent(null, ""));
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
		NoteContent arbitraryNoteContent = new NoteContent("arbitrary title", "test content of note");

		String postResponseBody = mockMvc.perform(post("/note").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(arbitraryNoteContent)))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();
		StorageId noteId = objectMapper.readValue(postResponseBody, StorageId.class);

		String getResponseBody = mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();
		Note[] retrievedNotes = objectMapper.readerFor(Note[].class).readValue(getResponseBody);

		assertThat(retrievedNotes).hasSize(1);
		assertThat(retrievedNotes[0])
				.usingRecursiveComparison()
				.isEqualTo(new Note(noteId, arbitraryNoteContent));
	}

	@Test
	void deleting_a_non_existing_note_should_give_back_404() throws Exception {
		mockMvc.perform(delete("/note/arbitraryNoteId")).andExpect(status().isNotFound());
	}

	@Test
	void posting_then_deleting_should_give_back_no_note() throws Exception {
		NoteContent arbitraryNoteContent = new NoteContent("arbitrary title", "test content of note to be deleted");

		String postResponseBody = mockMvc.perform(post("/note").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(arbitraryNoteContent)))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();

		StorageId noteId = objectMapper.readValue(postResponseBody, StorageId.class);

		String getResponseBody = mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		Note[] retrievedNotes = objectMapper.readerFor(Note[].class).readValue(getResponseBody);
		assertThat(retrievedNotes).hasSize(1);
		assertThat(retrievedNotes[0])
				.usingRecursiveComparison()
				.isEqualTo(new Note(noteId, arbitraryNoteContent));

		mockMvc.perform(MockMvcRequestBuilders.delete("/note/" + noteId.getId()))
				.andExpect(status().is2xxSuccessful());

		mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string("[]"));
	}

	@Test
	void putting_a_non_existing_note_should_give_back_404() throws Exception {
		NoteContent arbitraryNoteContent = new NoteContent(null, "modified test content of note");

		mockMvc.perform(put("/note").contentType(APPLICATION_JSON)
				.content(objectMapper
						.writeValueAsString(new UpdateNote(new StorageId("arbitraryNoteId", ""), arbitraryNoteContent))))
				.andExpect(status().isNotFound());
	}

	@Test
	void putting_an_existing_note_should_give_back_the_modified_note() throws Exception {
		NoteContent arbitraryInitialNoteContent = new NoteContent("arbitrary title",
				"test content of note to be modified");
		NoteContent arbitraryModifiedNoteContent = new NoteContent("modified arbitrary title",
				"MODIFIED test content of note");

		String postResponseBody = mockMvc.perform(post("/note").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(arbitraryInitialNoteContent)))
				.andExpect(status().is2xxSuccessful())
				.andReturn()
				.getResponse().getContentAsString();

		StorageId initialNoteId = objectMapper.readValue(postResponseBody, StorageId.class);

		String getResponseBeforeModificationBody = mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		Note[] firstRetrievedNotes = objectMapper.readerFor(Note[].class).readValue(getResponseBeforeModificationBody);
		assertThat(firstRetrievedNotes).hasSize(1);
		assertThat(firstRetrievedNotes[0])
				.usingRecursiveComparison()
				.isEqualTo(new Note(initialNoteId, arbitraryInitialNoteContent));

		mockMvc.perform(put(String.format("/note", initialNoteId.getId())).contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new UpdateNote(firstRetrievedNotes[0].getNoteId(), arbitraryModifiedNoteContent))))
				.andExpect(status().is2xxSuccessful());

		String getResponseAfterModificationBody = mockMvc.perform(get("/note"))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		Note[] secondRetrievedNotes = objectMapper.readerFor(Note[].class).readValue(getResponseAfterModificationBody);
		assertThat(secondRetrievedNotes).hasSize(1);
		assertThat(secondRetrievedNotes[0].getNoteId().getId()).isEqualTo(initialNoteId.getId());
		assertThat(secondRetrievedNotes[0].getContent())
				.usingRecursiveComparison()
				.isEqualTo(arbitraryModifiedNoteContent);
		assertThat(secondRetrievedNotes[0].getNoteId().getVersion()).isNotEqualTo(initialNoteId.getVersion());
	}
}
