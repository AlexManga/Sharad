package fr.ama.sharadback.service;

import static fr.ama.sharadback.service.StorageError.fileDoesNotExist;
import static fr.ama.sharadback.service.StorageError.genericFatalError;
import static fr.ama.sharadback.service.StorageError.storageServiceUnavailable;
import static fr.ama.sharadback.service.TagService.TagError.fromStorageError;
import static fr.ama.sharadback.service.TagService.TagErrorType.STORAGE_ERROR;
import static fr.ama.sharadback.utils.DirectoryUtils.createDirOrCheckAccess;
import static fr.ama.sharadback.utils.DirectoryUtils.generateUUID;
import static fr.ama.sharadback.utils.FatalError.fatalErrorSingleton;
import static fr.ama.sharadback.utils.Result.error;
import static fr.ama.sharadback.utils.Result.success;
import static fr.ama.sharadback.utils.ResultWithWarnings.errorWithoutWarnings;
import static fr.ama.sharadback.utils.ResultWithWarnings.partialResultWithWarnings;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.SetUtils.difference;
import static org.apache.commons.collections4.SetUtils.intersection;
import static org.apache.commons.collections4.SetUtils.unmodifiableSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.SetUtils.SetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;
import fr.ama.sharadback.utils.FatalError;
import fr.ama.sharadback.utils.Result;
import fr.ama.sharadback.utils.ResultWithWarnings;
import fr.ama.sharadback.utils.ResultWithWarnings.PartialResultWithWarnings;

@Service
public class TagService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);
	public static final StorageDomain STORAGE_DOMAIN = StorageDomain.SHARAD_TAGS;

	private static final String NO_ELEMENT_FOUND_MESSAGE = "No element found tagged with this tag";
	private static final String NOT_ALL_DELETED_SOME_UNTAGGED_MESSAGE = "Not all specified tagged elements were removed because they were found not to be tagged";

	private ObjectMapper objectMapper;
	private LocalStorageConfiguration localStorageConfiguration;

	public TagService(LocalStorageConfiguration localStorageConfiguration, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	public Result<TagError, StorageId> createTag(TagContent tagContent) {
		String tagId = generateUUID();
		return writeTagOnDisk(tagId, tagContent);
	}

	public List<Tag> getAllTags() {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!storageDir.exists()) {
			return List.of();
		}

		return stream(storageDir.listFiles(file -> file.isFile()))
				.map(this::retrieveTagFromFile)
				.filter(Result::isSuccess)
				.map(Result::getSuccess)
				.collect(toList());
	}

	public ResultWithWarnings<TagError, TagWarning, Optional<StorageId>> deleteTags(String tagId,
			List<String> taggedElements) {
		return readTagId(tagId)
				.map(previousTag -> deleteTags(previousTag, taggedElements))
				.onError(ResultWithWarnings::errorWithoutWarnings);
	}

	private ResultWithWarnings<TagError, TagWarning, Optional<StorageId>> deleteTags(Tag previousTag,
			List<String> elementsToDelete) {
		PartialResultWithWarnings<TagWarning> partialResult = partialResultWithWarnings();
		String[] stringArray = new String[] {};

		Set<String> taggedElementsIdsToDelete = unmodifiableSet(elementsToDelete.toArray(stringArray));
		Set<String> previousTaggedElementsIds = unmodifiableSet(
				previousTag.getContent().getTaggedElementsIds().toArray(stringArray));

		SetView<String> intersection = intersection(taggedElementsIdsToDelete, previousTaggedElementsIds);
		boolean nothingToDelete = intersection.isEmpty();
		if (nothingToDelete) {
			return errorWithoutWarnings(new TagError(TagErrorType.DELETE_NO_ELEMENT_TAGGED, NO_ELEMENT_FOUND_MESSAGE));
		} else if (!intersection.equals(taggedElementsIdsToDelete)) {
			partialResult = partialResultWithWarnings(
					new TagWarning(TagWarningType.NOT_ALL_DELETED_SOME_UNTAGGED,
							NOT_ALL_DELETED_SOME_UNTAGGED_MESSAGE));
		}

		return partialResult.fromResult(deleteElementsOrWholeTag(previousTag,
				difference(previousTaggedElementsIds, taggedElementsIdsToDelete)));
	}

	private Result<TagError, Optional<StorageId>> deleteElementsOrWholeTag(Tag previousTag,
			Set<String> newTaggedElements) {

		// TODO : Ne pas supprimer du disque
		boolean noMoreTaggedElements = newTaggedElements.isEmpty();
		if (noMoreTaggedElements) {
			deleteOnDisk(previousTag.getTagId().getId());
			return success(Optional.empty());
		} else {

			return overrideTaggedElementsOnDisk(previousTag, newTaggedElements)
					.map(Optional::of);
		}
	}

	private Result<TagError, StorageId> overrideTaggedElementsOnDisk(Tag previousTag,
			Set<String> newTaggedElements) {

		TagContent newTagContent = new TagContent(previousTag.getContent().getTag(),
				new ArrayList<>(newTaggedElements));

		return writeTagOnDisk(previousTag.getTagId().getId(), newTagContent);
	}

	private String buildTagFilename(String tagId) {
		return tagId + ".txt";
	}

	private Result<FatalError, String> computeVersion(TagContent tag) {
		MessageDigest digestAlgorithm;
		try {
			digestAlgorithm = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return error(fatalErrorSingleton());
		}
		return success(new String(digestAlgorithm.digest(computeVersionDataBytes(tag)), UTF_8));
	}

	private byte[] computeVersionDataBytes(TagContent tag) {
		return ("tag: " + tag.getTag() + ", taggedElements: [" + intersperse(tag.getTaggedElementsIds(), ", ") + "]")
				.getBytes(UTF_8);
	}

	private Result<TagError, Tag> readTagId(String tagId) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		File tagFile = new File(storageDir, buildTagFilename(tagId));
		return retrieveTagFromFile(tagFile);
	}

	private Result<TagError, Tag> retrieveTagFromFile(File file) {
		try {
			Tag tag = objectMapper.readValue(file, Tag.class);
			return success(tag);
		} catch (FileNotFoundException fnf) {
			LOGGER.warn(String.format("file not found: %s", file.getAbsolutePath()), fnf);
			return error(fromStorageError(fileDoesNotExist(file.getName())));
		} catch (IOException e) {
			LOGGER.warn(String.format("unable to read file %s", file.getAbsolutePath()), e);
			return Result.error(fromStorageError(genericFatalError(e)));
		}
	}

	private Result<TagError, StorageId> writeTagOnDisk(String id, TagContent newContent) {

		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!createDirOrCheckAccess(storageDir)) {
			return error(fromStorageError(
					storageServiceUnavailable()));
		}

		return computeVersion(newContent)
				.mapError(fatalErrorSingleton -> fromStorageError(genericFatalError(null)))
				.bind(noteVersion -> {
					File file = new File(storageDir, buildTagFilename(id));
					StorageId newId = new StorageId(id, noteVersion);
					try {
						objectMapper.writerFor(Tag.class).writeValue(file, new Tag(newId, newContent));
						return success(newId);
					} catch (IOException e) {
						return error(fromStorageError(
								genericFatalError(e)));
					}
				});
	}

	private Optional<TagError> deleteOnDisk(String id) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		if (!storageDir.exists()) {
			return Optional.of(fromStorageError(fileDoesNotExist(buildTagFilename(id))));
		}
		try {
			Files.delete(Paths.get(storageDir.getAbsolutePath(), buildTagFilename(id)));
			return Optional.empty();
		} catch (NoSuchFileException e) {
			return Optional.of(fromStorageError(fileDoesNotExist(buildTagFilename(id))));
		} catch (IOException e) {
			return Optional.of(fromStorageError(genericFatalError(e)));
		}
	}

	private String intersperse(List<String> strings, CharSequence inBetween) {
		if (strings.isEmpty()) {
			return "";
		}

		boolean foundAny = false;
		String result = null;
		for (String s : strings) {
			if (!foundAny) {
				foundAny = true;
				result = s;
			} else {
				result = result + inBetween + s;
			}
		}
		return result;
	}

	public static class TagError {
		private TagErrorType type;
		private String message;

		private TagError(TagErrorType type, String representation) {
			this.type = type;
			this.message = representation;
		}

		public static TagError fromStorageError(StorageError error) {
			String message = switch (error.getType()) {
			case FATAL: {
				yield "An unknown fatal error happened";
			}
			case FILE_DOES_NOT_EXIST: {
				yield "File does not exist";
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + error.getType());
			};

			return new TagError(STORAGE_ERROR, message);
		}

		public TagErrorType getType() {
			return type;
		}

		public String getMessage() {
			return message;
		}
	}

	public enum TagErrorType {
		DELETE_NO_ELEMENT_TAGGED, STORAGE_ERROR;
	}

	public static class TagWarning {
		private TagWarningType type;
		private String message;

		private TagWarning(TagWarningType type, String message) {
			this.type = type;
			this.message = message;
		}

		public TagWarningType getType() {
			return type;
		}

		public String getMessage() {
			return message;
		}
	}

	public enum TagWarningType {
		NOT_ALL_DELETED_SOME_UNTAGGED;
	}
}
