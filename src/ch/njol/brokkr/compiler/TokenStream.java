package ch.njol.brokkr.compiler;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Token.CommentToken;
import ch.njol.brokkr.compiler.Token.WhitespaceToken;

public class TokenStream {
	
	public final List<Token> tokens;
	
	int index = 0;
	
	public TokenStream(final List<Token> tokens) {
		this.tokens = tokens;
	}
	
	public @Nullable Token current() {
		if (index < 0 || index >= tokens.size())
			return null;
		return tokens.get(index);
	}
	
	public @Nullable Token forward() {
		if (index < 0 || index >= tokens.size())
			return null;
		return tokens.get(index++);
	}
	
	public @Nullable Token backward() {
		if (index < 0 || index >= tokens.size())
			return null;
		return tokens.get(index--);
	}
	
	public @Nullable Token peekNext(int delta, final boolean skipWhitespace) {
		assert delta >= 0;
		if (skipWhitespace) {
			@Nullable
			Token t = null;
			for (int i = 0; delta >= 0; i++) {
				if (index + i < 0 || index + i >= tokens.size())
					return null;
				t = tokens.get(index + i);
				if (!(t instanceof WhitespaceToken || t instanceof CommentToken))
					delta--;
			}
			return t;
		} else {
			if (index + delta < 0 || index + delta >= tokens.size())
				return null;
			return tokens.get(index + delta);
		}
	}
	
	/**
	 * Sets this stream to be at the given token.
	 * 
	 * @param t
	 */
	public void advanceTo(final Token t) {
		if (index >= 0 && index < tokens.size() && tokens.get(index) == t)
			return;
		final int oldIndex = index;
		index = tokens.indexOf(t);
		assert index > oldIndex : t;
	}
	
	public boolean isAfterEnd() {
		return index >= tokens.size();
	}
	
	public boolean isBeforeStart() {
		return index < 0;
	}
	
	// TODO store comments somewhere?
	public void skipWhitespace() {
		Token t;
		while (index < tokens.size() && ((t = tokens.get(index)) instanceof WhitespaceToken || t instanceof CommentToken))
			index++;
	}
	
	public int getTokenOffset() {
		return index;
	}
	
	public void setTokenOffset(final int index) {
		assert 0 <= index && index <= tokens.size() : index;
		this.index = index;
	}
	
	public void setTokenOffset(final Token t) {
//		index = Collections.binarySearch(tokens, t, (t1, t2) -> (t1.regionStart() - t2.regionStart()));
//		assert index >= 0 && tokens.get(index) == t;
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i) == t) {
				index = i;
				return;
			}
		}
		assert false : t;
	}
	
	public void setTextOffset(final int index) {
		assert 0 <= index && index <= getTextLength() : index + " / " + getTextLength();
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).regionStart() >= index) {
				this.index = tokens.get(i).regionStart() == index ? i : i - 1;
				return;
			}
		}
	}
	
	public int getTextOffset() {
		if (index < 0)
			return 0;
		if (index >= tokens.size())
			return tokens.size() == 0 ? 0 : tokens.get(tokens.size() - 1).regionEnd();
		return tokens.get(index).regionStart();
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
		return "TokenStream[textOffset: " + getTextOffset() + "; previous: " + (index <= 0 ? null : tokens.get(index - 1)) + "; current: " + current() + "; next: " + peekNext(1, false) + "]";
	}
	
}
