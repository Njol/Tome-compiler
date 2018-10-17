package ch.njol.tome.util;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.Token;

public class ASTTokenStream extends TokenStream {
	
	private @Nullable Token current;
	
	public ASTTokenStream(final ASTElementPart start) {
		current = getFirstToken(start);
	}
	
	private ASTTokenStream(@Nullable final Token current) {
		this.current = current;
	}
	
	@Override
	public ASTTokenStream clone() {
		return new ASTTokenStream(current);
	}
	
	private static @Nullable Token getFirstToken(final ASTElementPart ast) {
		if (ast instanceof Token)
			return (Token) ast;
		if (ast instanceof ASTElement) {
			final List<? extends ASTElementPart> parts = ((ASTElement) ast).parts();
			return parts.isEmpty() ? null : getFirstToken(parts.get(0));
		}
		assert false : ast.getClass();
		return null;
	}
	
	@Override
	public @Nullable Token current() {
		return current;
	}
	
	@Override
	public @Nullable Token moveForwardAndGet() {
		moveForward();
		return current;
	}
	
	@Override
	public @Nullable Token getAndMoveForward() {
		final Token current = this.current;
		moveForward();
		return current;
	}
	
	@Override
	public void moveForward() {
		moveBy(1);
	}
	
	@Override
	public @Nullable Token moveBackwardAndGet() {
		moveBackward();
		return current;
	}
	
	@Override
	public @Nullable Token getAndMoveBackward() {
		final Token current = this.current;
		moveBackward();
		return current;
	}
	
	@Override
	public void moveBackward() {
		moveBy(-1);
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
		final List<? extends ASTElementPart> parts = parent.parts();
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
			final ASTElement grandParent = parent.parent();
			if (grandParent == null)
				return delta;
			return move(parent, grandParent, delta);
		}
		return delta;
	}
	
	public String toDebugString() {
		final Token oldCurrent = current;
		final StringBuilder b = new StringBuilder();
		Token t;
		while ((t = getAndMoveForward()) != null) {
			b.append("" + t);
		}
		current = oldCurrent;
		return b.toString();
	}
	
}
