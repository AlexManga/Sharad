package fr.ama.sharadback.service;

import static fr.ama.sharadback.service.StorageError.fileDoesNotExist;
import static fr.ama.sharadback.service.StorageError.genericFatalError;
import static fr.ama.sharadback.service.TagService.TagError.fromStorageError;
import static fr.ama.sharadback.service.TagService.TagErrorType.STORAGE_ERROR;
import static fr.ama.sharadback.utils.DirectoryUtils.createDirOrCheckAccess;
import static fr.ama.sharadback.utils.DirectoryUtils.generateUUID;
import static fr.ama.sharadback.utils.Result.error;
import static fr.ama.sharadback.utils.Result.success;
import static fr.ama.sharadback.utils.ResultWithWarnings.partialResultWithWarnings;
import static fr.ama.sharadback.utils.ResultWithWarnings.successWithoutWarnings;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.SetUtils.difference;
import static org.apache.commons.collections4.SetUtils.intersection;
import static org.apache.commons.collections4.SetUtils.unmodifiableSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ama.sharadback.controller.FatalException;
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;
import fr.ama.sharadback.utils.Result;
import fr.ama.sharadback.utils.ResultWithWarnings;
import fr.ama.sharadback.utils.ResultWithWarnings.PartialResultWithWarnings;

@Service
public class TagService {
	private static final String NO_ELEMENT_FOUND_MESSAGE = "No element found tagged with this tag";
	private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);
	public static final StorageDomain STORAGE_DOMAIN = StorageDomain.SHARAD_TAGS;
	private static final String NOT_ALL_DELETED_SOME_UNTAGGED_MESSAGE = "Not all specified tagged elements were removed because they were found not to be tagged";

	private ObjectMapper objectMapper;
	private LocalStorageConfiguration localStorageConfiguration;

	@Autowired
	public TagService(LocalStorageConfiguration localStorageConfiguration, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.localStorageConfiguration = localStorageConfiguration;
	}

	public StorageId createTag(TagContent tagContent) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		createDirOrCheckAccess(storageDir);

		String tagId = generateUUID();
		return writeTagOnDisk(tagContent, tagId);
	}

	private StorageId writeTagOnDisk(TagContent tagContent, String tagId) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		try (FileOutputStream fos = new FileOutputStream(new File(storageDir, buildTagFilename(tagId)))) {
			String tagVersion = computeVersion(tagContent);
			fos.write(objectMapper
					.writeValueAsBytes(new Tag(new StorageId(tagId, tagVersion), tagContent)));
			return new StorageId(tagId, tagVersion);
		} catch (Exception e) {
			throw new FatalException("Unknown error happened", e);
		}
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

	private String buildTagFilename(String tagId) {
		return tagId + ".txt";
	}

	private String computeVersion(TagContent tag) throws NoSuchAlgorithmException {
		return new String(MessageDigest.getInstance("SHA-256").digest(computeBytesVersion(tag)), UTF_8);
	}

	private byte[] computeBytesVersion(TagContent tag) {
		return ("tag: " + tag.getTag() + ", taggedElements: [" + intersperse(tag.getTaggedElementsIds(), ", ") + "]")
				.getBytes(UTF_8);
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

	public ResultWithWarnings<TagError, TagWarning, Optional<StorageId>> deleteTags(String tagId,
			List<String> taggedElements) {
		// StorageErrors should come from here
		Result<TagError, Tag> readTag = readTag(tagId);

		return readTag
				.map(previousTag -> doDeleteTags(taggedElements, previousTag))
				.onError(ResultWithWarnings::errorWithoutWarnings);
	}

	private ResultWithWarnings<TagError, TagWarning, Optional<StorageId>> doDeleteTags(List<String> taggedElements,
			Tag previousTag) {
		String[] stringArray = new String[] {};
		Set<String> taggedElementsIds = unmodifiableSet(taggedElements.toArray(stringArray));
		Set<String> previousTaggedElementsIds = unmodifiableSet(
				previousTag.getContent().getTaggedElementsIds().toArray(stringArray));

		SetView<String> intersection = intersection(taggedElementsIds, previousTaggedElementsIds);
		PartialResultWithWarnings<TagWarning> partialResult = partialResultWithWarnings();
		if (!intersection.equals(taggedElementsIds)) {
			if (intersection.isEmpty()) {
				return ResultWithWarnings.errorWithoutWarnings(
						new TagError(TagErrorType.DELETE_NO_ELEMENT_TAGGED, NO_ELEMENT_FOUND_MESSAGE));
			}
			partialResult = ResultWithWarnings.partialResultWithWarnings(new TagWarning(
					TagWarningType.NOT_ALL_DELETED_SOME_UNTAGGED, NOT_ALL_DELETED_SOME_UNTAGGED_MESSAGE));
		}

		SetView<String> newTaggedElements = difference(previousTaggedElementsIds, taggedElementsIds);

		if (newTaggedElements.isEmpty()) {
			deleteOnDisk(previousTag.getTagId().getId());
			return successWithoutWarnings(Optional.empty());
		} else {

			TagContent newTagContent = new TagContent(previousTag.getContent().getTag(),
					new ArrayList<>(newTaggedElements));

			StorageId storageId = writeTagOnDisk(newTagContent, previousTag.getTagId().getId());

			return partialResult.success(Optional.of(storageId));
		}
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

	private Result<TagError, Tag> readTag(String tagId) {
		File storageDir = localStorageConfiguration.getPathFor(STORAGE_DOMAIN).toFile();
		File tagFile = new File(storageDir, buildTagFilename(tagId));
		return retrieveTagFromFile(tagFile);
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
