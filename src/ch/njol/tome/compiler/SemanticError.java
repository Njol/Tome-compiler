package ch.njol.tome.compiler;

public final class SemanticError {
	
	public final String message;
	
	public final int start, length;
	
	public SemanticError(final String message, final int start, final int length) {
		this.message = message;
		this.start = start;
		this.length = length;
	}
	
	@Override
	public String toString() {
		return message;
	}
	
}
