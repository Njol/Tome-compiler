package ch.njol.tome.interpreter;

/**
 * Thrown if anything went wrong in the interpreter.
 */
public class InterpreterException extends Exception {
	private static final long serialVersionUID = -115983341907607591L;
	
	public InterpreterException(final String message) {
		super(message);
	}
	
}
