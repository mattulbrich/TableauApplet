package de.uka.ilkd.tablet;

@SuppressWarnings("serial")
public class ApplyException extends Exception {

	public ApplyException() {
	}

	public ApplyException(String message) {
		super(message);
	}

	public ApplyException(Throwable cause) {
		super(cause);
	}

	public ApplyException(String message, Throwable cause) {
		super(message, cause);
	}

}
