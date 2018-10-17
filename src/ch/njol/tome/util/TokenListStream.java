package ch.njol.tome.util;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CommentToken;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;
import ch.njol.tome.compiler.Token.WhitespaceToken;
import ch.njol.tome.compiler.TokenList;

public class TokenListStream extends TokenStream {
	
	public final TokenList tokens;
	
	private int tokenIndex = 0;
	private int characterIndex = 0;
	
	public TokenListStream(final TokenList tokens) {
		this.tokens = tokens;
	}
	
	private TokenListStream(final TokenList tokens, final int tokenIndex, final int characterIndex) {
		this.tokens = tokens;
		this.tokenIndex = tokenIndex;
		this.characterIndex = characterIndex;
	}
	
	@Override
	public TokenListStream clone() {
		return new TokenListStream(tokens, tokenIndex, characterIndex);
	}
	
	/**
	 * @return The token at the current position
	 */
	@Override
	public @Nullable Token current() {
		if (tokenIndex < 0 || tokenIndex >= tokens.size())
			return null;
		return tokens.get(tokenIndex);
	}
	
	/**
	 * @return The token at the current position
	 */
	@Override
	public @Nullable Token getAndMoveForward() {
		if (tokenIndex < 0 || tokenIndex >= tokens.size())
			return null;
		final Token t = tokens.get(tokenIndex);
		tokenIndex++;
		characterIndex += t.regionLength();
		return t;
	}
	
	/**
	 * @return The token at the next position
	 */
	@Override
	public @Nullable Token moveForwardAndGet() {
		if (tokenIndex < 0 || tokenIndex >= tokens.size())
			return null;
		characterIndex += tokens.get(tokenIndex).regionLength();
		tokenIndex++;
		return tokenIndex >= tokens.size() ? null : tokens.get(tokenIndex);
	}
	
	@Override
	public void moveForward() {
		getAndMoveForward();
	}
	
	/**
	 * @return The token at the current position
	 */
	@Override
	public @Nullable Token getAndMoveBackward() {
		if (tokenIndex < 0 || tokenIndex >= tokens.size())
			return null;
		final Token t = tokens.get(tokenIndex);
		tokenIndex--;
		if (tokenIndex >= 0)
			characterIndex -= tokens.get(tokenIndex).regionLength();
		return t;
	}
	
	/**
	 * @return The token at the previous position
	 */
	@Override
	public @Nullable Token moveBackwardAndGet() {
		if (tokenIndex < 0 || tokenIndex >= tokens.size())
			return null;
		tokenIndex--;
		final Token t = tokenIndex < 0 ? null : tokens.get(tokenIndex);
		if (t != null)
			characterIndex -= t.regionLength();
		return t;
	}
	
	@Override
	public void moveBackward() {
		moveBackwardAndGet();
	}
	
	/**
	 * Resets this stream to the beginning
	 */
	public void reset() {
		tokenIndex = 0;
		characterIndex = 0;
	}
	
	public @Nullable Token peekNext(int delta, final boolean skipWhitespace) {
		assert delta >= 0;
		if (skipWhitespace) {
			@Nullable
			Token t = null;
			for (int i = 0; delta >= 0; i++) {
				if (tokenIndex + i < 0 || tokenIndex + i >= tokens.size())
					return null;
				t = tokens.get(tokenIndex + i);
				if (!(t instanceof WhitespaceToken || t instanceof CommentToken))
					delta--;
			}
			return t;
		} else {
			if (tokenIndex + delta < 0 || tokenIndex + delta >= tokens.size())
				return null;
			return tokens.get(tokenIndex + delta);
		}
	}
	
//	/**
//	 * Sets this stream to be at the given token.
//	 *
//	 * @param t
//	 */
//	public void advanceTo(final Token t) {
//		if (index >= 0 && index < tokens.size() && tokens.get(index) == t)
//			return;
//		final int oldIndex = index;
//		index = tokens.indexOf(t);
//		assert index > oldIndex : t;
//	}
	
	public boolean isAfterEnd() {
		return tokenIndex >= tokens.size();
	}
	
	public boolean isBeforeStart() {
		return tokenIndex < 0;
	}
	
	public void skipWhitespace(final Consumer<WhitespaceOrCommentToken> whiteSpaceConsumer) {
		Token t;
		while (tokenIndex < tokens.size() && ((t = tokens.get(tokenIndex)) instanceof WhitespaceOrCommentToken)) {
			whiteSpaceConsumer.accept((WhitespaceOrCommentToken) t);
			tokenIndex++;
			characterIndex += t.regionLength();
		}
	}
	
	public int getTokenOffset() {
		return tokenIndex;
	}
	
	public void setTokenOffset(final int index) {
		assert 0 <= index && index <= tokens.size() : index;
		tokenIndex = index;
		characterIndex = 0;
		for (int i = 0; i < index; i++) {
			characterIndex += tokens.get(i).regionLength();
		}
	}
	
	public void setTokenOffset(final Token t) {
		characterIndex = 0;
		for (int i = 0; i < tokens.size(); i++) {
			final Token token = tokens.get(i);
			if (token == t) {
				tokenIndex = i;
				return;
			}
			characterIndex += token.regionLength();
		}
		assert false : t;
	}
	
	/**
	 * Advances to the token at the given text position. If the given index is between two tokens, the later token (whose start is equal to the given index) is chosen.
	 * 
	 * @param index
	 */
	public void setTextOffset(final int index) {
		assert 0 <= index : index;
		characterIndex = 0;
		if (index == 0) { // special case: 0 is allowed if token list is empty
			tokenIndex = 0;
			return;
		}
		for (int i = 0; i < tokens.size(); i++) {
			final Token token = tokens.get(i);
			if (characterIndex <= index) {
				tokenIndex = i;
				return;
			}
			characterIndex += token.regionLength();
		}
		assert false : index + "/" + characterIndex;
	}
	
	public int getTextOffset() {
		return characterIndex;
	}
	
	public int getTextOffsetAfterCurrentToken() {
		if (tokenIndex < 0)
			return 0;
		if (tokenIndex >= tokens.size())
			return characterIndex; // should be at the end
		return characterIndex + tokens.get(tokenIndex).regionLength();
	}
	
	public int getTextLength() {
		return tokens.tokens.stream().mapToInt(t -> t.regionLength()).sum();
	}
	
}
