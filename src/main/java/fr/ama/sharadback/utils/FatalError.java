package fr.ama.sharadback.utils;

public class FatalError {

	private static FatalError instance;

	private FatalError() {
	}

	public static FatalError fatalErrorSingleton() {
		if (instance == null) {
			instance = new FatalError();
		}
		return instance;
	}

}
