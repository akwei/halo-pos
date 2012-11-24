package halo.pos;

public class PosMissFieldException extends RuntimeException {

	private static final long serialVersionUID = 6332664711079485371L;

	public PosMissFieldException() {
		super();
	}

	public PosMissFieldException(String message, Throwable cause) {
		super(message, cause);
	}

	public PosMissFieldException(String message) {
		super(message);
	}

	public PosMissFieldException(Throwable cause) {
		super(cause);
	}
}
