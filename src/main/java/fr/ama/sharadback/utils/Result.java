package fr.ama.sharadback.utils;

import java.util.function.Function;

public interface Result<E, S> {

	boolean isSuccess();

	default boolean isError() {
		return !isSuccess();
	}

	S getSuccess();

	E getError();

	default <T> Result<E, T> map(Function<S, T> mapper) {
		if (isSuccess())
			return Result.<E, T>success(mapper.apply(getSuccess()));

		return Result.<E, T>error(getError());
	}

	default <T> Result<E, T> bind(Function<S, Result<E, T>> binding) {
		if (isSuccess()) {
			return binding.apply(getSuccess());
		}
		return Result.<E, T>error(getError());
	}

	default S onError(Function<E, S> errorToSuccessMapper) {
		if (isSuccess())
			return getSuccess();

		return errorToSuccessMapper.apply(getError());
	}

	public static <E, S> Result<E, S> success(S success) {
		return new Success<E, S>(success);
	}

	public static <E, S> Result<E, S> error(E error) {
		return new Error<E, S>(error);
	}

	static class Success<E, S> implements Result<E, S> {

		private S success;

		public Success(S success) {
			this.success = success;
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public S getSuccess() {
			return success;
		}

		@Override
		public E getError() {
			throw new IllegalStateException("Cannot get error from success");
		}
	}

	static class Error<E, S> implements Result<E, S> {

		private E error;

		public Error(E error) {
			this.error = error;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public S getSuccess() {
			throw new IllegalStateException("Cannot get success from error");
		}

		@Override
		public E getError() {
			return error;
		}
	}

}
