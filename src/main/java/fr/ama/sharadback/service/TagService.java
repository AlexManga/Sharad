package fr.ama.sharadback.service;

import static fr.ama.sharadback.utils.DirectoryUtils.createDirOrCheckAccess;
import static fr.ama.sharadback.utils.DirectoryUtils.generateUUID;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import fr.ama.sharadback.model.storage.StorageId;
import fr.ama.sharadback.model.tag.Tag;
import fr.ama.sharadback.model.tag.TagContent;

@Service
public class TagService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);
	public static final StorageDomain STORAGE_DOMAIN = StorageDomain.SHARAD_TAGS;

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
				.filter(Optional::isPresent)
				.map(Optional::get)
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

	private Optional<Tag> retrieveTagFromFile(File file) {
		try {
			Tag tag = objectMapper.readValue(file, Tag.class);
			return Optional.of(tag);
		} catch (IOException e) {
			LOGGER.warn(String.format("unable to read file %s", file.getAbsolutePath()), e);
			return Optional.empty();
		}
	}
}
