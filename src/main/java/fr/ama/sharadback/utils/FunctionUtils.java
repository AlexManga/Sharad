package fr.ama.sharadback.utils;

import java.util.function.Function;

public class FunctionUtils {
	public static final <E, T> Function<E, T> constant(T constantVal) {
		return a -> constantVal;
	}
}
