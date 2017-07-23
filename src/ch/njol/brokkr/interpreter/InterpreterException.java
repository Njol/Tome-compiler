package ch.njol.brokkr.interpreter;

/**
 * Thrown if anything went wrong in the interpreter. Currently, {@link NullPointerException}s are used often instead of this one. // TODO fix
 */
public class InterpreterException extends RuntimeException {
	private static final long serialVersionUID = -115983341907607591L;
	
	public InterpreterException(final String message) {
		super(message);
	}
}
