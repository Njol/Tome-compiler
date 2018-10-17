package ch.njol.tome.compiler;

import org.eclipse.jdt.annotation.Nullable;

public class StringReader implements SourceReader {
	
	private String s;
	
	public StringReader(final String s) {
		this.s = s;
	}
	
	public void setString(final String s) {
		this.s = s;
	}
	
	int offset = 0;
	
	@Override
	public int getOffset() {
		return offset;
	}
	
	@Override
	public void setOffset(final int offset) {
		this.offset = offset;
	}
	
	@Override
	public void back() {
		offset--;
	}
	
	@Override
	public void reset() {
		offset = 0;
	}
	
	@Override
	public boolean isBeforeStart() {
		return offset <= 0;
	}
	
	@Override
	public boolean isAfterEnd() {
		return offset >= s.length();
	}
	
	@Override
	public int getLine(final int offset) {
		if (offset < 0 || offset >= s.length())
			return -1;
		int r = 0;
		for (int i = 0; i < offset; i++)
			if (s.charAt(i) == '\n')
				r++;
		return r;
	}
	
	private int scan(final char c, final int start, final int direction, final int defaultValue) {
		for (int i = start; 0 <= i && i < s.length(); i += direction) {
			if (s.charAt(i) == c)
				return i;
		}
		return defaultValue;
	}
	
	@Override
	public String getLineTextAtOffset(final int offset) {
		return "" + s.substring(getLineStart(offset), getLineEnd(offset));
	}
	
	@Override
	public int getLineStart(final int offset) {
		return scan('\n', offset, -1, -1) + 1;
	}
	
	@Override
	public int getLineEnd(final int offset) {
		return scan('\n', offset, 1, offset < 0 ? 0 : s.length());
	}
	
	@Override
	public int getColumn(final int offset) {
		if (offset < 0 || offset >= s.length())
			return -1;
		return offset - (scan('\n', offset, -1, -1) + 1);
	}
	
	@Override
	public String getText(final int start, final int end) {
		return s.substring(start, end);
	}
	
	@Override
	public int peekNext() {
		if (offset < 0 || offset >= s.length())
			return -1;
		return s.charAt(offset);
	}
	
	@Override
	public int peekNext(final int delta) {
		if (offset + delta < 0 || offset + delta >= s.length())
			return -1;
		return s.charAt(offset + delta);
	}
	
	@Override
	public int next() {
		final int next = peekNext();
		if (next != -1)
			offset++;
		return next;
	}
	
	@Override
	public int getLength() {
		return s.length();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + s.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final StringReader other = (StringReader) obj;
		if (!s.equals(other.s))
			return false;
		return true;
	}
	
}
