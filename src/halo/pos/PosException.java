package halo.pos;

public class PosException extends Exception {

	private static final long serialVersionUID = -6136759447973814877L;

	public PosException() {
		super();
	}

	public PosException(String message, Throwable cause) {
		super(message, cause);
	}

	public PosException(String message) {
		super(message);
	}

	public PosException(Throwable cause) {
		super(cause);
	}
}
