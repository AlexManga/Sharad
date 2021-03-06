package fr.ama.sharadback.utils;

import static java.util.List.copyOf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ResultWithWarnings<E, W, S> implements Result<E, S> {

	private Result<E, S> underlyingResult;
	private List<W> warnings = List.of();

	private ResultWithWarnings(Result<E, S> underlyingResult, List<W> warnings) {
		this.underlyingResult = underlyingResult;
		this.warnings = warnings;
	}

	@Override
	public boolean isSuccess() {
		return underlyingResult.isSuccess();
	}

	@Override
	public S getSuccess() {
		return underlyingResult.getSuccess();
	}

	@Override
	public E getError() {
		return underlyingResult.getError();
	}

	public List<W> getWarnings() {
		return warnings;
	}

	@SuppressWarnings("unchecked")
	public ResultWithWarnings<E, W, S> withWarning(W firstWarning, W... warnings) {
		LinkedList<W> newWarnings = new LinkedList<>(this.getWarnings());
		newWarnings.addAll((List<W>) Arrays.asList(firstWarning, warnings));
		return new ResultWithWarnings<>(this.underlyingResult, newWarnings);
	}

	public <T> ResultWithWarnings<E, W, T> mapWithWarnings(BiFunction<List<W>, S, T> mapper) {
		return new ResultWithWarnings<>(underlyingResult.map(s -> mapper.apply(this.warnings, s)), this.warnings);
	}

	public <X> ResultWithWarnings<E, X, S> transformWarnings(Function<List<W>, List<X>> mapper) {
		return new ResultWithWarnings<>(underlyingResult, mapper.apply(copyOf(this.warnings)));
	}

	public static <E, W, S> ResultWithWarnings<E, W, S> successWithoutWarnings(S success) {
		return success(success, List.of());
	}

	public static <E, W, S> ResultWithWarnings<E, W, S> errorWithoutWarnings(E error) {
		return error(error, List.of());
	}

	public static <E, W, S> ResultWithWarnings<E, W, S> success(S success, List<W> warnings) {
		return new ResultWithWarnings<E, W, S>(Result.success(success), warnings);
	}

	public static <E, W, S> ResultWithWarnings<E, W, S> error(E error, List<W> warnings) {
		return new ResultWithWarnings<E, W, S>(Result.error(error), warnings);
	}

	@SuppressWarnings("unchecked")
	public static <W> PartialResultWithWarnings<W> partialResultWithWarnings(W... warnings) {
		return new PartialResultWithWarnings<>((List<W>) Arrays.asList(warnings));
	}

	public static class PartialResultWithWarnings<W> {
		private List<W> warnings;

		private PartialResultWithWarnings(List<W> warnings) {
			this.warnings = warnings;
		}

		public <E, S> ResultWithWarnings<E, W, S> success(S success) {
			return new ResultWithWarnings<E, W, S>(Result.success(success), this.warnings);
		}

		public <E, S> ResultWithWarnings<E, W, S> error(E error) {
			return new ResultWithWarnings<E, W, S>(Result.error(error), this.warnings);
		}
	}

}
