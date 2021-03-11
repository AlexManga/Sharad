package fr.ama.sharadback.utils;

import static java.util.List.copyOf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Result<E, W, S> {

	boolean isSuccess();

	default boolean isError() {
		return !isSuccess();
	}

	S getSuccess();

	E getError();

	List<W> getWarnings();

	default <T> Result<E, W, T> map(Function<S, T> mapper) {
		if (isSuccess())
			return Result.<E, W, T>success(mapper.apply(getSuccess()));

		return Result.<E, W, T>error(getError());
	}

	default <Z> Result<E, Z, S> ingnoreWarnings() {
		if (isSuccess()) {
			return new Success<E, Z, S>(getSuccess());
		}
		return new Error<E, Z, S>(getError());
	}

	default <T> Result<E, W, T> bind(Function<S, Result<E, W, T>> binding) {
		if (isSuccess()) {
			return binding.apply(getSuccess());
		}
		return Result.<E, W, T>error(getError());
	}

	default S onError(Function<E, S> errorToSuccessMapper) {
		if (isSuccess())
			return getSuccess();

		return errorToSuccessMapper.apply(getError());
	}

	default <F> Result<F, W, S> mapError(Function<E, F> mapper) {
		if (this.isError()) {
			return new Error<F, W, S>(mapper.apply(getError()));
		}
		return new Success<F, W, S>(this.getSuccess());
	}

	default <Z> Result<E, Z, S> mapWarnings(Function<List<W>, List<Z>> mapper) {
		if (this.isError()) {
			return new Error<E, Z, S>(getError());
		}
		return new Success<E, Z, S>(this.getSuccess(), mapper.apply(copyOf(getWarnings())));
	}

	default <T> Result<E, W, T> mapWithWarnings(BiFunction<List<W>, S, T> mapper) {
		if (isSuccess()) {
			return new Success<E, W, T>(mapper.apply(getWarnings(), getSuccess()), getWarnings());
		}
		return new Error<E, W, T>(getError());
	}

	@SuppressWarnings("unchecked")
	default Result<E, W, S> withWarning(W firstWarning, W... warnings) {
		if (isSuccess()) {
			LinkedList<W> newWarnings = new LinkedList<>(this.getWarnings());
			newWarnings.addAll((List<W>) Arrays.asList(firstWarning, warnings));
			return new Success<E, W, S>(getSuccess(), newWarnings);
		}
		return new Error<E, W, S>(getError());
	}

	default Result<E, W, S> withWarning(List<W> warnings) {
		if (isSuccess()) {
			return new Success<E, W, S>(getSuccess(), warnings);
		}
		return new Error<E, W, S>(getError());
	}

	public static <E, W, S> Result<E, W, S> success(S success) {
		return new Success<E, W, S>(success);
	}

	public static <E, W, S> Result<E, W, S> error(E error) {
		return new Error<E, W, S>(error);
	}

	@SafeVarargs
	public static <W> Result.PartialResult<W> partialResultWithWarnings(W... warnings) {
		return new PartialResult<>((List<W>) Arrays.asList(warnings));
	}

	static class Success<E, W, S> implements Result<E, W, S> {

		private S success;
		private List<W> warnings = List.of();

		public Success(S success) {
			this.success = success;
		}

		public Success(S success, List<W> warnings) {
			this.success = success;
			this.warnings = warnings;
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
		public List<W> getWarnings() {
			return warnings;
		}

		@Override
		public E getError() {
			throw new IllegalStateException("Cannot get error from success");
		}

	}

	static class Error<E, W, S> implements Result<E, W, S> {

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

		@Override
		public List<W> getWarnings() {
			throw new IllegalStateException("Cannot have warnings with an error");
		}
	}

	class PartialResult<W> {
		private List<W> warnings = List.of();

		PartialResult(List<W> warnings) {
			this.warnings = warnings;
		}

		public <E, S> Result<E, W, S> success(S success) {
			return new Success<E, W, S>(success, this.warnings);
		}

		public <E, S> Result<E, W, S> error(E error) {
			return new Error<E, W, S>(error);
		}

		public <E, S> Result<E, W, S> fromResult(Result<E, W, S> result) {
			if (this.warnings.isEmpty()) {
				return result;
			}
			return result.withWarning(this.warnings);
		}

		public <E, S> Result<E, W, S> fromResultWithoutWarnings(Result<E, Void, S> result) {
			return result.mapWarnings(FunctionUtils.constant(this.warnings));
		}
	}

}
