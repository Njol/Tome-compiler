package ch.njol.brokkr.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public class TokenList implements Iterable<Token> {

	public final List<Token> tokens;

	public TokenList(final List<Token> tokens) {
		this.tokens = Collections.unmodifiableList(tokens);
	}
	
	public TokenStream stream() {
		return new TokenStream(tokens);
	}

	public @Nullable Token getTokenAt(final int characterOffset, final boolean tieToRight) {
		if (characterOffset < 0 || tokens.isEmpty())
			return null;
		if (characterOffset == 0)
			return tokens.get(0);
		for (final Token t : tokens) { // TODO more efficient (e.g. binary search)
			if (tieToRight) {
				if (t.regionStart() <= characterOffset && characterOffset < t.regionEnd())
					return t;
			} else {
				if (t.regionStart() < characterOffset && characterOffset <= t.regionEnd())
					return t;
			}
		}
		if (characterOffset == tokens.get(tokens.size() - 1).regionEnd())
			return tokens.get(tokens.size() - 1);
		return null;
	}
	
	public @Nullable Token getTokenBefore(Token otherToken) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i) == otherToken) {
				if (i == 0)return null;
				return tokens.get(i-1);
			}
		}
		return null;
	}
	
	public int getTextLength() {
		return tokens.size() == 0 ? 0 : tokens.get(tokens.size() - 1).regionEnd();
	}
	
	@Override
	public String toString() {
		return "TokenList";
	}

	@Override
	public Iterator<Token> iterator() {
		return tokens.iterator();
	}
	
}
