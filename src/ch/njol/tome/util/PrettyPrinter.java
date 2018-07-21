package ch.njol.tome.util;

import java.io.IOException;

public class PrettyPrinter {
	
	private final Appendable out;
	private final String indentation;
	private int currentIndentation = 0;
	
	public PrettyPrinter(final Appendable out, final String indentation) {
		this.out = out;
		this.indentation = indentation;
	}
	
	private final void append(final String text) {
		try {
			out.append(text);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void printIndentation() {
		for (int i = 0; i < currentIndentation; i++)
			append(indentation);
	}
	
	public void print(final String text) {
		append(text);
	}
	
	public void printLine(final String line) {
		append(currentIndentation + line + "\n");
	}
	
	public void increaseIndentation() {
		currentIndentation++;
	}
	
	public void decreaseIndentation() {
		currentIndentation--;
	}
	
	public void changeIndentation(final int delta) {
		currentIndentation += delta;
	}
	
}
