package ch.njol.tome.compiler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.common.AbstractInvalidatable;
import ch.njol.tome.parser.ParseError;
import ch.njol.tome.util.PrettyPrinter;
import ch.njol.util.StringUtils;

public interface Token extends ASTElementPart {
	
	public default List<ParseError> errors() {
		return Collections.EMPTY_LIST;
	}
	
	public abstract class AbstractToken implements Token {
		
		private @Nullable ASTElement parent = null;
		
		@Override
		public @Nullable ASTElement parent() {
			return parent;
		}
		
		@Override
		public void setParent(@Nullable final ASTElement parent) {
			ASTElementPart.assertValidParent(this, parent);
			if (this.parent != parent)
				this.parent = parent;
		}
		
		@Override
		public void print(final PrettyPrinter out) {
			out.print("" + this);
		}
		
		@Override
		public abstract String toString();
		
		@Override
		public final boolean equals(@Nullable final Object obj) {
			return super.equals(obj);
		}
		
		@Override
		public final int hashCode() {
			return super.hashCode();
		}
		
		/**
		 * Compares tokens according to their type and content.
		 */
		public final boolean dataEquals(final Token other) {
			if (getClass() != other.getClass())
				return false;
			return dataEquals_(other);
		}
		
		public abstract int dataHashCode();
		
		/**
		 * @param t Token of the exact same class as this one.
		 * @return
		 */
		protected abstract boolean dataEquals_(Token t);
		
	}
	
	public abstract static class WordToken extends AbstractToken implements WordOrSymbols {
		
		public final String word;
		
		public boolean keyword;
		
		public WordToken(final String word) {
			this.word = word;
		}
		
		@Override
		public String toString() {
			return word;
		}
		
		@Override
		public String wordOrSymbols() {
			return word;
		}
		
		@Override
		public int regionLength() {
			return word.length();
		}
		
		@Override
		public List<WordToken> tokens() {
			return Collections.singletonList(this);
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return word.equals(((WordToken) t).word);
		}
		
		@Override
		public int dataHashCode() {
			return word.hashCode();
		}
		
		@Override
		public boolean isKeyword() {
			return keyword;
		}
		
	}
	
	public final static class UppercaseWordToken extends WordToken {
		public UppercaseWordToken(final String word) {
			super(word);
		}
	}
	
	public final static class LowercaseWordToken extends WordToken {
		public LowercaseWordToken(final String word) {
			super(word);
		}
	}
	
	public static interface WordOrSymbols extends ASTElementPart {
		
		String wordOrSymbols();
		
		List<? extends Token> tokens();
		
		boolean isKeyword();
		
	}
	
	public final static class SymbolsWord extends AbstractInvalidatable implements WordOrSymbols, ASTElement {
		
		public final List<SymbolToken> symbols;
		private final String joined;
		
		private @Nullable ASTElement parent;
		
		public SymbolsWord(final List<SymbolToken> symbols) {
			joined = StringUtils.join(symbols);
			this.symbols = symbols;
			for (final SymbolToken t : symbols) {
				t.removeFromParent();
				t.setParent(this);
			}
		}
		
		@Override
		public String wordOrSymbols() {
			return joined;
		}
		
		@Override
		public String toString() {
			return joined;
		}
		
		@Override
		public List<SymbolToken> tokens() {
			return symbols;
		}
		
		@Override
		public @Nullable ASTElement parent() {
			return parent;
		}
		
		@Override
		public void setParent(@Nullable final ASTElement parent) {
			ASTElementPart.assertValidParent(this, parent);
			this.parent = parent;
		}
		
		@Override
		public void print(final PrettyPrinter out) {
			out.print(joined);
		}
		
		@Override
		public List<? extends ASTElementPart> parts() {
			return Collections.unmodifiableList(symbols);
		}
		
		@Override
		public void addLink(final ASTLink<?> link) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public List<ASTLink<?>> links() {
			return Collections.emptyList();
		}
		
		@Override
		public void invalidateSelf() {
			invalidate();
		}
		
		@Override
		public boolean isKeyword() {
			return false;
		}
		
		@Override
		public int regionLength() {
			return joined.length();
		}
		
		@Override
		public void insertChild(final ASTElementPart child, final int index) {
			assert false;
		}
		
		@Override
		public void removeChild(final ASTElementPart child) {
			assert false;
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
	
	public final static class NumberToken extends AbstractToken {
		
		public final String code;
		public final BigDecimal value;
		
		private final List<ParseError> errors;
		
		public NumberToken(final String code, final BigDecimal value, final List<ParseError> errors) {
			this.code = code;
			this.value = value;
			this.errors = errors;
		}
		
		@Override
		public List<ParseError> errors() {
			return errors;
		}
		
		@Override
		public String toString() {
			if (value.scale() <= 0) // integer
				return "" + value + " (0x" + value.toBigInteger().toString(16).toUpperCase(Locale.ENGLISH) + ")";
			else
				return "" + value;
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return value.compareTo(((NumberToken) t).value) == 0; // BigDecimal.equal is incorrect
		}
		
		@Override
		public int regionLength() {
			return code.length();
		}
		
		@Override
		public int dataHashCode() {
			return value.intValue(); // BigDecimal.hashCode is incorrect
		}
	}
	
	public final static class StringToken extends AbstractToken {
		
		public final String code;
		public final String value;
		
		private final List<ParseError> errors;
		
		public StringToken(final String code, final String value, final List<ParseError> errors) {
			this.code = code;
			this.value = value;
			this.errors = errors;
		}
		
		@Override
		public List<ParseError> errors() {
			return errors;
		}
		
		@Override
		public String toString() {
			return '\'' + value.replaceAll("'", "\\'") + '\'';
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return value.equals(((StringToken) t).value);
		}
		
		@Override
		public int regionLength() {
			return code.length();
		}
		
		@Override
		public int dataHashCode() {
			return value.hashCode();
		}
		
	}
	
	public final static class CodeGenerationToken extends AbstractToken {
		
		public final String code; // FIXME check uses
		public final String parsed;
		public final boolean ended;
		
		public CodeGenerationToken(final String code, final String parsed, final boolean ended) {
			this.code = code;
			this.parsed = parsed;
			this.ended = ended;
		}
		
		@Override
		public String toString() {
			return "$" + parsed.replaceAll("$", "$$") + "$";
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return parsed.equals(((CodeGenerationToken) t).parsed);
		}
		
		@Override
		public int regionLength() {
			return code.length();
		}
		
		@Override
		public int dataHashCode() {
			return parsed.hashCode();
		}
		
	}
	
	public static interface WhitespaceOrCommentToken extends Token {
		
	}
	
	public final static class WhitespaceToken extends AbstractToken implements WhitespaceOrCommentToken {
		
		public final String value;
		
		public WhitespaceToken(final String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return value.equals(((WhitespaceToken) t).value);
		}
		
		@Override
		public int regionLength() {
			return value.length();
		}
		
		@Override
		public int dataHashCode() {
			return value.hashCode();
		}
		
	}
	
	public static class SymbolToken extends AbstractToken implements WordOrSymbols {
		
		public final char symbol;
		
		public SymbolToken(final char symbol) {
			this.symbol = symbol;
		}
		
		@Override
		public String toString() {
			return "" + symbol;
		}
		
		@Override
		public String wordOrSymbols() {
			return "" + symbol;
		}
		
		@Override
		public List<SymbolToken> tokens() {
			return Collections.singletonList(this);
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return symbol == ((SymbolToken) t).symbol;
		}
		
		@Override
		public boolean isKeyword() {
			return false;
		}
		
		@Override
		public int regionLength() {
			return 1;
		}
		
		@Override
		public int dataHashCode() {
			return symbol;
		}
		
	}
	
	public static abstract class CommentToken extends AbstractToken implements WhitespaceOrCommentToken {
		
		/**
		 * Comment including comment markers
		 */
		public final String comment;
		
		public CommentToken(final String comment) {
			this.comment = comment;
		}
		
		@Override
		protected boolean dataEquals_(final Token t) {
			return comment.equals(((CommentToken) t).comment);
		}
		
		@Override
		public String toString() {
			return comment;
		}
		
		/**
		 * @return A pretty version of this comment. For single-line comments, this just removes the leading "//" and any extra whitespace after that or at the end of the line.
		 *         For multi-line comments, see {@link MultiLineCommentToken#toPrettyString()}.
		 */
		public abstract String toPrettyString();
		
		@Override
		public int regionLength() {
			return comment.length();
		}
		
		@Override
		public int dataHashCode() {
			return comment.hashCode();
		}
		
	}
	
	public final static class SingleLineCommentToken extends CommentToken {
		public SingleLineCommentToken(final String comment) {
			super(comment);
		}
		
		@Override
		public String toPrettyString() {
			return comment.substring("//".length()).trim();
		}
	}
	
	public final static class MultiLineCommentToken extends CommentToken {
		public MultiLineCommentToken(final String comment) {
			super(comment);
		}
		
		private final static Pattern prettyStringRemovalPattern = Pattern.compile("\\s*\n\\s*\\*?\\s*");
		
		/**
		 * Returns a pretty version of this comment. Removes the leading "/*" and trailing "*﻿/", then trims whitespace from each line,
		 * then removes an optional * from the start of each line (as well as following whitespace), then joins the lines with a space inbetween (ignoring empty lines).
		 */
		@Override
		public String toPrettyString() {
			return prettyStringRemovalPattern.matcher(comment.substring(2, comment.length() - 2)).replaceAll(" ").trim();
		}
	}
	
}
