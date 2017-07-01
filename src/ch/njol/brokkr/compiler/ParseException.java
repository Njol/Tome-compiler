package ch.njol.brokkr.compiler;

public class ParseException extends Exception {
	private static final long serialVersionUID = -7066345621377092435L;
	
	// does not have an error - set that before throwing the exception!
	public ParseException() {
		super("", null, false, false);
	}
	
}
