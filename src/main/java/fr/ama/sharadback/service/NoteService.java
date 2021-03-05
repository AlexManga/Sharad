package fr.ama.sharadback.service;

import static fr.ama.sharadback.service.StorageError.genericFatalError;
import static fr.ama.sharadback.utils.DirectoryUtils.createDirOrCheckAccess;
import static fr.ama.sharadback.utils.DirectoryUtils.generateUUID;
import static fr.ama.sharadback.utils.Result.error;
import static fr.ama.sharadback.utils.Result.success;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.controller.FatalException;
import fr.ama.sharadback.model.note.Note;
import fr.ama.sharadback.model.note.NoteContent;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.utils.Result;

@Service
public class NoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoteService.class);
	public static final StorageDomain STORAGE_DOMAIN = StorageDomain.SHARAD_NOTES;

	private LocalStorageConfiguration localStorageConfiguration;
	private ObjectMapper objectMapper;

	@Autowired
	public NoteService(LocalStorageConfiguration localStorageConfiguration, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	public StorageId createNote(NoteContent noteContent) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		createDirOrCheckAccess(storageDir);

		String noteId = generateUUID();
		try (FileOutputStream fos = new FileOutputStream(new File(storageDir, buildNoteFilename(noteId)))) {
			String noteVersion = computeVersion(noteContent);
			fos.write(objectMapper
					.writeValueAsBytes(new Note(new StorageId(noteId, noteVersion), noteContent)));
			return new StorageId(noteId, noteVersion);
		} catch (Exception e) {
			throw new FatalException("Unknown error happened", e);
		}
	}

	public List<Note> getAllNotes() {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
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
		File fileToDelete = new File(localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile(), fileName);
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

	public Result<StorageError, StorageId> modifyNote(StorageId previousNoteId, NoteContent newContent) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}

		if (!storageDir.isDirectory() || !storageDir.canRead() || !storageDir.canWrite()) {
			return error(
					StorageError.fileDoesNotExist(localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toString()));
		}

		String noteFileName = buildNoteFilename(previousNoteId.getId());
		File noteFileToModify = new File(storageDir, noteFileName);
		if (!noteFileToModify.exists()) {
			return error(StorageError.fileDoesNotExist(noteFileToModify.getPath()));
		}

		try (FileOutputStream fos = new FileOutputStream(
				new File(storageDir, noteFileName))) {
			String noteVersion = computeVersion(newContent);
			fos.write(objectMapper
					.writeValueAsBytes(new Note(new StorageId(previousNoteId.getId(), noteVersion), newContent)));
			return success(new StorageId(previousNoteId.getId(), noteVersion));
		} catch (Exception e) {
			return error(genericFatalError(e));
		}
	}

	private String computeVersion(NoteContent content) throws NoSuchAlgorithmException {
		return new String(MessageDigest.getInstance("SHA-256").digest(content.getBody().getBytes(UTF_8)), UTF_8);
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
}
