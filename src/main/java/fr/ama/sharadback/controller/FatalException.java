package fr.ama.sharadback.controller;

public class FatalException extends RuntimeException {
	private static final long serialVersionUID = -5812707702712433751L;
	
	public FatalException(String message) {
		super(message);
	}

}
