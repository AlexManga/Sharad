package fr.ama.sharadback.service;

import static fr.ama.sharadback.service.StorageError.fileDoesNotExist;
import static fr.ama.sharadback.service.StorageError.genericFatalError;
import static fr.ama.sharadback.utils.DirectoryUtils.createDirOrCheckAccess;
import static fr.ama.sharadback.utils.DirectoryUtils.generateUUID;
import static fr.ama.sharadback.utils.FatalError.fatalErrorSingleton;
import static fr.ama.sharadback.utils.Result.error;
import static fr.ama.sharadback.utils.Result.success;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.note.Note;
import fr.ama.sharadback.model.note.NoteContent;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.utils.FatalError;
import fr.ama.sharadback.utils.Result;

@Service
public class NoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoteService.class);
	public static final StorageDomain STORAGE_DOMAIN = StorageDomain.SHARAD_NOTES;

	private LocalStorageConfiguration localStorageConfiguration;
	private ObjectMapper objectMapper;

	public NoteService(LocalStorageConfiguration localStorageConfiguration, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	public Result<StorageError, Void, StorageId> createNote(NoteContent noteContent) {
		String noteId = generateUUID();
		return writeNoteOnDisk(noteId, noteContent);
	}

	public List<Note> getAllNotes() {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!storageDir.exists()) {
			return List.of();
		}

		return stream(storageDir.listFiles(file -> file.isFile()))
				.map(this::retrieveNoteFromFile)
				.filter(Result::isSuccess)
				.map(Result::getSuccess)
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
			return Optional.of(genericFatalError(e));
		}

	}

	public Result<StorageError, Void, StorageId> modifyNote(StorageId previousNoteId, NoteContent newContent) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();

		File noteFileToModify = new File(storageDir, buildNoteFilename(previousNoteId.getId()));
		if (!noteFileToModify.exists()) {
			return error(fileDoesNotExist(noteFileToModify.getPath()));
		}

		return writeNoteOnDisk(previousNoteId.getId(), newContent);
	}

	private Result<StorageError, Void, StorageId> writeNoteOnDisk(String id, NoteContent newContent) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!createDirOrCheckAccess(storageDir)) {
			return error(StorageError.storageServiceUnavailable());
		}

		return computeVersion(newContent)
				.mapError(fatalErrorSingleton -> (StorageError) error(genericFatalError(null)))
				.bind(noteVersion -> {
					File file = new File(storageDir, buildNoteFilename(id));
					StorageId newId = new StorageId(id, noteVersion);
					Note note = new Note(newId, newContent);
					try {
						objectMapper.writerFor(Note.class).writeValue(file, note);
						return success(newId);
					} catch (IOException e) {
						return error(genericFatalError(e));
					}
				});
	}

	private Result<StorageError, Void, Note> retrieveNoteFromFile(File file) {
		try {
			Note note = objectMapper.readValue(file, Note.class);
			return success(note);
		} catch (FileNotFoundException fnf) {
			LOGGER.warn(String.format("file not found: %s", file.getAbsolutePath()), fnf);
			return error(fileDoesNotExist(file.getName()));
		} catch (IOException e) {
			LOGGER.warn(String.format("unable to read file %s", file.getAbsolutePath()), e);
			return error(genericFatalError(e));
		}
	}

	private Result<FatalError, Void, String> computeVersion(NoteContent content) {
		MessageDigest digestAlgorithm;
		try {
			digestAlgorithm = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return error(fatalErrorSingleton());
		}
		return success(new String(digestAlgorithm.digest(content.getBody().getBytes(UTF_8)), UTF_8));
	}

	private String buildNoteFilename(String fileId) {
		return fileId + ".txt";
	}
}
