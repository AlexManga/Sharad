package fr.ama.sharadback.service;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.delete;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.ama.sharadback.SharadBackApplication;
import fr.ama.sharadback.model.note.Note;
import fr.ama.sharadback.model.note.NoteContent;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.utils.Result;
import fr.ama.sharadback.utils.StreamUtils;

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
		File rootStorage = localStorageConfiguration.getPathFor(NoteService.STORAGE_DOMAIN).toFile();
		if (rootStorage.exists()) {
			delete(rootStorage);
		}
		assertThat(localStorageConfiguration.getPathFor(NoteService.STORAGE_DOMAIN).toFile()).doesNotExist();
	}

	@Test
	void creating_a_note_should_create_a_file() {
		NoteContent arbitraryNoteContent = new NoteContent("arbitrary title", "arbitrary note content");
		StorageId noteId = noteService.createNote(arbitraryNoteContent).getSuccess();

		File expectedFile = new File(localStorageConfiguration.getPathFor(NoteService.STORAGE_DOMAIN).toFile(),
				noteId.getId() + ".txt");
		assertThat(expectedFile).exists();
	}

	@Test
	void getting_all_notes_on_empty_storage_should_give_back_empty_list() throws Exception {
		List<Note> notes = noteService.getAllNotes();

		assertThat(notes).isEmpty();
	}

	@Test
	void getting_notes_after_creating_some_should_give_them_back() throws Exception {
		// TODO : Stream.of
		Stream<String> titles = asList(new String[] { "title 1", "title 2", "title 3" })
				.stream();
		Stream<String> contents = asList(new String[] { "content 1", "content 2", "content 3" })
				.stream();

		List<NoteContent> arbitraryContents = StreamUtils.zip(titles, contents)
				.map(titleAndContent -> new NoteContent(titleAndContent.getFirst(), titleAndContent.getSecond()))
				.collect(toList());
		List<StorageId> createdNotes = arbitraryContents
				.stream()
				.map(noteService::createNote)
				.map(Result::getSuccess)
				.collect(toList());

		List<Note> retrievedNotes = noteService.getAllNotes();

		assertThat(retrievedNotes.stream().map(Note::getNoteId))
				.usingFieldByFieldElementComparator()
				.containsAll(createdNotes);

		assertThat(retrievedNotes.stream()
				.map(Note::getContent)
				.collect(toList()))
						.usingFieldByFieldElementComparator()
						.containsAll(arbitraryContents);
	}

}
