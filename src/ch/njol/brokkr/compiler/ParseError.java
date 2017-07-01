package ch.njol.brokkr.compiler;

public class ParseError {
	
	public final static String EXPECTED = "Expected ";
	
	public final String message;
	
	public final int start, length;
	
//	StackTraceElement[] stackTrace;
	
	public ParseError(final String message, final int start, final int length) {
		assert start >= 0 && length > 0 : message + "; " + start + "; " + length;
		this.message = message;
		this.start = start;
		this.length = length;
//		stackTrace = Thread.currentThread().getStackTrace();
	}
	
//	public void printStackTrace() {
//		for (final StackTraceElement e : stackTrace)
//			System.out.println(" at " + e);
//	}
	
	@Override
	public String toString() {
//		final int i = 2;
//		while (stackTrace[i].getClassName().endsWith(".Element$AbstractElement") || stackTrace[i].getClassName().contains(".Literals")) i++;
		return toString_();// + " [ " + stackTrace[i] + " ]";
	}
	
	private String toString_() {
		return message;
	}
	
}
