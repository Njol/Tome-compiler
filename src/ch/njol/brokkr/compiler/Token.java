package ch.njol.brokkr.compiler;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTLink;
import ch.njol.util.StringUtils;

public abstract class Token implements ASTElementPart {
	
	public @Nullable List<ParseError> errors = null;
	
	public Token(final int start, final int end) {
		assert 0 <= start && start < end : start + " - " + end;
		this.start = start;
		this.end = end;
	}
	
	private @Nullable ASTElement parent = null;
	
	@Override
	public @Nullable ASTElement parent() {
		return parent;
	}
	
	@Override
	public void setParent(@Nullable final ASTElement parent) {
		assert parent != this;
		if (this.parent != parent) {
			if (this.parent != null) {
				this.parent.parts().remove(this);
				assert this.parent != null;
				if (errors != null)
					this.parent.fatalParseErrors().removeAll(errors);
			}
			if (parent != null) {
				parent.parts().add(this);
				if (errors != null)
					parent.fatalParseErrors().addAll(errors);
			}
			this.parent = parent;
		}
	}
	
	private final int start;
	private final int end;
	
	@Override
	public final int regionStart() {
		return start;
	}
	
	@Override
	public final int regionEnd() {
		return end;
	}
	
	@Override
	public final int regionLength() {
		return end - start;
	}
	
	@Override
	public void print(final PrintStream out, final String indent) {
		out.println("" + this);
	}
	
	@Override
	public abstract String toString();
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;
		final Token t = (Token) obj;
		if (t.start != start || t.end != end)
			return false;
		return dataEquals(t);
	}
	
	@Override
	public int hashCode() {
		return 0; // correct but useless
	}
	
	/**
	 * @param t Token of the exact same class as this one.
	 * @return
	 */
	protected abstract boolean dataEquals(Token t);
	
	public abstract static class WordToken extends Token {
		
		public final String word;
		
		public boolean keyword;
		
		public WordToken(final String word, final int start, final int end) {
			super(start, end);
			this.word = word;
		}
		
		@Override
		public String toString() {
			return word;
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return word.equals(((WordToken) t).word);
		}
	}
	
	public final static class UppercaseWordToken extends WordToken {
		public UppercaseWordToken(final String word, final int start, final int end) {
			super(word, start, end);
		}
	}
	
	public final static class LowercaseWordToken extends WordToken {
		public LowercaseWordToken(final String word, final int start, final int end) {
			super(word, start, end);
		}
	}
	
	/**
	 * Fake token to be used in {@link ASTLink Links} of operators.
	 */
	public final static class SymbolsWordToken extends WordToken {
		public final List<SymbolToken> symbols;
		
		public SymbolsWordToken(final List<SymbolToken> symbols) {
			super(StringUtils.join(symbols), symbols.get(0).regionStart(), symbols.get(symbols.size() - 1).regionEnd());
			this.symbols = symbols;
		}
		
		public boolean contains(final SymbolToken singleSymbol) {
			return symbols.contains(singleSymbol);
		}
	}
	
//	public final static class KeywordToken extends Token {
//
//		public final String word;
//
//		public KeywordToken(final String word, final int start, final int end) {
//			super(start, end);
//			this.word = word;
//		}
//
//		@Override
//		public String toString() {
//			return word;
//		}
//
//		@Override
//		protected boolean dataEquals(@final Token t) {
//			return word.equals(((KeywordToken) t).word);
//		}
//	}
	
	public final static class NumberToken extends Token {
		
		public final BigDecimal value;
		
		public NumberToken(final BigDecimal value, final List<ParseError> errors, final int start, final int end) {
			super(start, end);
			this.value = value;
			this.errors = errors;
		}
		
		@Override
		public String toString() {
			if (value.scale() <= 0) // integer
				return "" + value + " (0x" + value.toBigInteger().toString(16).toUpperCase(Locale.ENGLISH) + ")";
			else
				return "" + value;
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return value.equals(((NumberToken) t).value);
		}
	}
	
	public final static class StringToken extends Token {
		
		public final String value;
		
		public StringToken(final String value, final List<ParseError> errors, final int start, final int end) {
			super(start, end);
			this.value = value;
			this.errors = errors;
		}
		
		@Override
		public String toString() {
			return '\'' + value.replaceAll("'", "\\'") + '\'';
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return value.equals(((StringToken) t).value);
		}
	}
	
	public final static class CodeGenerationToken extends Token {
		
		public final String code;
		public final boolean ended;
		
		public CodeGenerationToken(final String code, final boolean ended, final int start, final int end) {
			super(start, end);
			this.code = code;
			this.ended = ended;
		}
		
		@Override
		public String toString() {
			return "$" + code.replaceAll("'", "\\'") + "$";
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return code.equals(((CodeGenerationToken) t).code);
		}
	}
	
	public final static class WhitespaceToken extends Token {
		
		public final String value;
		
		public WhitespaceToken(final String value, final int start, final int end) {
			super(start, end);
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return value.equals(((WhitespaceToken) t).value);
		}
	}
	
	public static class SymbolToken extends Token {
		
		public final char symbol;
		
		public SymbolToken(final char symbol, final int start, final int end) {
			super(start, end);
			this.symbol = symbol;
		}
		
		@Override
		public String toString() {
			return "" + symbol;
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return symbol == ((SymbolToken) t).symbol;
		}
	}
	
	public static abstract class CommentToken extends Token {
		
		public final String comment;
		
		public CommentToken(final String comment, final int start, final int end) {
			super(start, end);
			this.comment = comment;
		}
		
		@Override
		protected boolean dataEquals(final Token t) {
			return comment.equals(((CommentToken) t).comment);
		}
		
		@Override
		public String toString() {
			return comment;
		}
	}
	
	public final static class SingleLineCommentToken extends CommentToken {
		public SingleLineCommentToken(final String comment, final int start, final int end) {
			super(comment, start, end);
		}
	}
	
	public final static class MultiLineCommentToken extends CommentToken {
		public MultiLineCommentToken(final String comment, final int start, final int end) {
			super(comment, start, end);
		}
	}
	
}
