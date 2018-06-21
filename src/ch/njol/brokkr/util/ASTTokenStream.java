package ch.njol.brokkr.util;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.compiler.Token;

public class ASTTokenStream {
	
	private @Nullable Token current;
	
	public ASTTokenStream(final ASTElementPart start) {
		current = getFirstToken(start);
	}
	
	private static @Nullable Token getFirstToken(final ASTElementPart ast) {
		if (ast instanceof Token)
			return (Token) ast;
		if (ast instanceof ASTElement) {
			final List<ASTElementPart> parts = ((ASTElement) ast).parts();
			return parts.isEmpty() ? null : getFirstToken(parts.get(0));
		}
		assert false : ast.getClass();
		return null;
	}
	
	public @Nullable Token getCurrent() {
		return current;
	}
	
	public @Nullable Token next() {
		moveBy(1);
		return getCurrent();
	}
	
	public @Nullable Token previous() {
		moveBy(-1);
		return getCurrent();
	}
	
	/**
	 * Moves this {@link ASTTokenStream} by the given number of tokens. If there are not enough tokens, this stream becomes invalid.
	 * 
	 * @param delta How many tokens to move by
	 */
	public void moveBy(final int delta) {
		if (delta == 0)
			return;
		final Token current = this.current;
		if (current == null)
			return;
		this.current = null; // set to null here instead of everywhere where it's not found
		final ASTElement parent = current.parent();
		if (parent == null)
			return;
		move(current, parent, delta);
	}
	
	private int move(final @Nullable ASTElementPart current, final ASTElement parent, int delta) {
		final List<ASTElementPart> parts = parent.parts();
		if (parts.isEmpty())
			return delta;
		final int dir = delta > 0 ? 1 : -1;
		for (int currentIndex = current == null ? (delta < 0 ? parts.size() - 1 : 0) : parts.indexOf(current) + dir; 0 <= currentIndex && currentIndex < parts.size(); currentIndex += dir) {
			final ASTElementPart part = parts.get(currentIndex);
			if (part instanceof Token) {
				delta -= dir;
				if (delta == 0) {
					this.current = (Token) part;
					return 0;
				}
			} else if (part instanceof ASTElement) {
				delta = move(null, (ASTElement) part, delta);
				if (delta == 0)
					return 0;
			} else {
				assert false : part.getClass();
			}
		}
		if (delta != 0) {
			ASTElement grandParent = parent.parent();
			if (grandParent == null)
				return delta;
			return move(parent, grandParent, delta);
		}
		return delta;
	}
	
}
