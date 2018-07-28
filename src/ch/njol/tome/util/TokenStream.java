package ch.njol.tome.util;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.Token;

public abstract class TokenStream {
	
	@Override
	public abstract TokenStream clone();
	
	public abstract @Nullable Token current();
	
	public abstract @Nullable Token moveForwardAndGet();
	
	public abstract @Nullable Token getAndMoveForward();
	
	public abstract void moveForward();
	
	public abstract @Nullable Token moveBackwardAndGet();
	
	public abstract @Nullable Token getAndMoveBackward();
	
	public abstract void moveBackward();
	
	@Override
	public String toString() {
		final TokenStream backwards = clone(), forwards = clone();
		final StringBuilder b = new StringBuilder("║" + current() + "║");
		final int maxTokensPerDirection = 20;
		for (int i = 0; i < maxTokensPerDirection; i++) {
			final Token previous = backwards.moveBackwardAndGet();
			if (previous != null)
				b.insert(0, "│" + previous);
			final Token next = forwards.moveForwardAndGet();
			if (next != null)
				b.append(next + "│");
		}
		return b.toString();
	}
	
}
