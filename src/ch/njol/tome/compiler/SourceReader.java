package ch.njol.tome.compiler;

public interface SourceReader {
	
	/**
	 * @return The offset between the previous and the next character (i.e. the zero-based index of the next character)
	 */
	public int getOffset();
	
	public void setOffset(int offset);
	
	public default int peekNext() {
		return peekNext(0);
	}
	
	public boolean isBeforeStart();
	
	public boolean isAfterEnd();
	
	public int getLine(int offset);
	
	public int getColumn(int offset);
	
	public String getLineTextAtOffset(int offset);
	
	public int getLineStart(int offset);
	
	public int getLineEnd(int offset);
	
	/**
	 * Same as {@link #peekNext()}, but looks ahead 'delta' characters (where 0 = next).
	 * 
	 * @param delta
	 * @return
	 */
	public int peekNext(int delta);
	
	public int next();
	
	public void back();
	
	public void reset();
	
	public int getLength();
	
	public default void skipWhitespace() {
		while (Character.isWhitespace(peekNext()))
			next();
	}
	
	public String getText(int start, int end);
	
}
