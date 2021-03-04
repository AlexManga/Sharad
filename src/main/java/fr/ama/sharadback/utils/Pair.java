package fr.ama.sharadback.utils;

public class Pair<L, R> {

	private L first;
	private R second;

	public Pair(L left, R right) {
		this.first = left;
		this.second = right;
	}

	public L getFirst() {
		return first;
	}

	public R getSecond() {
		return second;
	}

}
