package fr.ama.sharadback.service;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.controller.FatalException;
import fr.ama.sharadback.model.Note;
import fr.ama.sharadback.model.NoteContent;
import fr.ama.sharadback.model.NoteId;

@Service
public class NoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoteService.class);

	private LocalStorageConfiguration localStorageConfiguration;
	private ObjectMapper objectMapper;

	@Autowired
	public NoteService(LocalStorageConfiguration localStorageConfiguration, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	public NoteId createNote(NoteContent noteContent) {
		File storageDir = new File(localStorageConfiguration.getRootPath());
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}

		if (!storageDir.isDirectory() || !storageDir.canRead() || !storageDir.canWrite()) {
			throw new FatalException(String.format("unable to access storageDir %s", storageDir.getAbsolutePath()));
		}

		String noteId = generateNoteId();
		try (FileOutputStream fos = new FileOutputStream(new File(storageDir, buildNoteFilename(noteId)))) {
			fos.write(objectMapper.writeValueAsBytes(new Note(noteId, noteContent.getContent())));
			return new NoteId(noteId);
		} catch (IOException e) {
			throw new FatalException("Couldn't write note in file");
		}
	}

	public List<Note> getAllNotes() {
		File storageDir = new File(localStorageConfiguration.getRootPath());
		if (!storageDir.exists()) {
			return List.of();
		}

		return stream(storageDir.listFiles(file -> file.isFile()))
				.map(this::retrieveNoteFromFile)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}

	public Optional<StorageError> deleteNote(String noteId) {
		String fileName = buildNoteFilename(noteId);
		File fileToDelete = new File(localStorageConfiguration.getRootPath(), fileName);
		if (!fileToDelete.exists()) {
			return Optional.of(StorageError.fileDoesNotExist(noteId));
		}

		try {
			Files.delete(Paths.get(fileToDelete.getAbsolutePath()));
			return Optional.empty();
		} catch (IOException e) {
			return Optional.of(StorageError.genericFatalError(e));
		}

	}

	private Optional<Note> retrieveNoteFromFile(File file) {
		try {
			Note note = objectMapper.readValue(file, Note.class);
			return Optional.of(note);
		} catch (IOException e) {
			LOGGER.warn(String.format("unable to read file %s", file.getAbsolutePath()), e);
			return Optional.empty();
		}
	}

	private String buildNoteFilename(String fileId) {
		return fileId + ".txt";
	}

	private String generateNoteId() {
		return UUID.randomUUID().toString();
	}
}
