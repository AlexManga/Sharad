package fr.ama.sharadback.utils;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {

	/**
	 * Note: this implementation does not support parallel execution. See:
	 * https://stackoverflow.com/questions/24059837/iterate-two-java-8-streams-together
	 * 
	 * @param <F>     type of the first component of the Pair
	 * @param <S>     type of the second component of the Pair
	 * @param firsts
	 * @param seconds
	 * @return a stream of Pairs containing one element of firsts and the element in
	 *         seconds at the same place. The size of the resulting stream is the
	 *         smallest size between firsts and seconds
	 */
	public static <F, S> Stream<Pair<F, S>> zip(Stream<F> firsts, Stream<S> seconds) {
		Iterator<F> i1 = firsts.iterator();
		Iterator<S> i2 = seconds.iterator();
		Iterable<Pair<F, S>> i = () -> new Iterator<Pair<F, S>>() {
			public boolean hasNext() {
				return i1.hasNext() && i2.hasNext();
			}

			public Pair<F, S> next() {
				return new Pair<F, S>(i1.next(), i2.next());
			}
		};
		return StreamSupport.stream(i.spliterator(), false);
	}

}
