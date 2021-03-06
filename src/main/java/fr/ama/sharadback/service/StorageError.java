package fr.ama.sharadback.service;

public class StorageError {

	private Type type;

	public StorageError(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public static enum Type {
		FILE_DOES_NOT_EXIST, FATAL, STORAGE_SERVICE_UNAVAILABLE;
	}

	public static FileDoesNotExistError fileDoesNotExist(String fileName) {
		return new FileDoesNotExistError(fileName);
	}

	public static FatalError genericFatalError(Exception exception) {
		return new FatalError(exception);
	}

	public static StorageServiceUnavailable storageServiceUnavailable() {
		return new StorageServiceUnavailable();
	}

	public static class FileDoesNotExistError extends StorageError {

		private String file;

		private FileDoesNotExistError(String file) {
			super(Type.FILE_DOES_NOT_EXIST);
			this.file = file;
		}

		public String getFile() {
			return file;
		}
	}

	public static class FatalError extends StorageError {

		private Exception exception;

		public FatalError(Exception excpetion) {
			super(Type.FATAL);
			this.exception = excpetion;
		}

		public Exception getException() {
			return exception;
		}

	}

	public static class StorageServiceUnavailable extends StorageError {
		private StorageServiceUnavailable() {
			super(Type.STORAGE_SERVICE_UNAVAILABLE);
		}
	}

}
