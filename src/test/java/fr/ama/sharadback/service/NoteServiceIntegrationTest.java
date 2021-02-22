package fr.ama.sharadback.service;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.delete;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.ama.sharadback.SharadBackApplication;
import fr.ama.sharadback.model.Note;
import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;

@SpringBootTest(classes = { SharadBackApplication.class })
@ActiveProfiles("test")
public class NoteServiceIntegrationTest {

	private LocalStorageConfiguration localStorageConfiguration;
	private NoteService noteService;

	@Autowired
	public NoteServiceIntegrationTest(LocalStorageConfiguration localStorageConfiguration,
			NoteService noteService) {
		this.localStorageConfiguration = localStorageConfiguration;
		this.noteService = noteService;
	}

	@BeforeEach
	private void before() throws IOException {
		File rootStorage = new File(localStorageConfiguration.getRootPath());
		if (rootStorage.exists()) {
			delete(rootStorage);
		}
		assertThat(new File(localStorageConfiguration.getRootPath())).doesNotExist();
	}

	@Test
	void creating_a_note_should_create_a_file() throws Exception {
		NoteContent arbitraryNoteContent = new NoteContent("arbitrary note content");
		NoteId noteId = noteService.createNote(arbitraryNoteContent);

		File expectedFile = new File(localStorageConfiguration.getRootPath(), noteId.getId() + ".txt");
		assertThat(expectedFile).exists();
	}

	@Test
	void getting_all_notes_on_empty_storage_should_give_back_empty_list() throws Exception {
		List<Note> notes = noteService.getAllNotes();

		assertThat(notes).isEmpty();
	}

	@Test
	void getting_notes_after_creating_some_should_give_them_back() throws Exception {
		List<String> arbitraryContents = asList(new String[] { "content 1", "content 2", "content 3" });
		List<NoteId> createdNotes = arbitraryContents.stream()
				.map(content -> noteService.createNote(new NoteContent(content)))
				.collect(toList());

		List<Note> retrievedNotes = noteService.getAllNotes();

		assertThat(retrievedNotes.stream().map(Note::getNoteId))
				.usingFieldByFieldElementComparator()
				.containsAll(createdNotes);

		assertThat(retrievedNotes.stream().map(Note::getContent))
				.containsAll(arbitraryContents);
	}

}
