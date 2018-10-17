package ch.njol.tome.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.util.TokenListStream;
import ch.njol.util.StringUtils;

public class TokenList implements Iterable<Token> {
	
	public final List<Token> tokens;
	
	public TokenList(final List<Token> tokens) {
		this.tokens = Collections.unmodifiableList(tokens);
	}
	
	public TokenListStream stream() {
		return new TokenListStream(this);
	}
	
	public int size() {
		return tokens.size();
	}
	
	public boolean isEmpty() {
		return tokens.isEmpty();
	}
	
	public Token get(final int index) {
		return tokens.get(index);
	}
	
	public ListIterator<Token> listIterator() {
		return tokens.listIterator();
	}
	
	public @Nullable Token getTokenAt(final int characterOffset, final boolean tieToRight) {
		if (characterOffset < 0 || tokens.isEmpty())
			return null;
		if (characterOffset == 0)
			return tokens.get(0);
		int currentIndex = 0;
		for (final Token t : tokens) {
			final int length = t.regionLength();
			final int nextIndex = currentIndex + length;
			if (tieToRight) {
				if (currentIndex <= characterOffset && characterOffset < nextIndex)
					return t;
			} else {
				if (currentIndex < characterOffset && characterOffset <= nextIndex)
					return t;
			}
			currentIndex = nextIndex;
		}
		if (characterOffset == currentIndex) // exactly the ending index
			return tokens.get(tokens.size() - 1);
		return null;
	}
	
	public @Nullable Token getTokenBefore(final Token otherToken) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i) == otherToken) {
				if (i == 0)
					return null;
				return tokens.get(i - 1);
			}
		}
		return null;
	}
	
//	public int getTextLength() {
//		return tokens.size() == 0 ? 0 : tokens.get(tokens.size() - 1).regionEnd();
//	}
	
	@Override
	public String toString() {
		return "│" + StringUtils.join(tokens, "│") + "│";
	}
	
	@Override
	public Iterator<Token> iterator() {
		return tokens.iterator();
	}
	
	@Override
	public int hashCode() {
		return tokens.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj instanceof TokenList) {
			final List<Token> tokens2 = ((TokenList) obj).tokens;
			if (tokens.size() != tokens2.size())
				return false;
			for (int i = 0; i < tokens.size(); i++) {
				if (!tokens.get(i).dataEquals(tokens2.get(i)))
					return false;
			}
			return true;
		}
		return false;
	}
	
}
