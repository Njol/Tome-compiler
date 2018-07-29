package ch.njol.tome.compiler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.Constants;
import ch.njol.tome.compiler.Token.CodeGenerationToken;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.MultiLineCommentToken;
import ch.njol.tome.compiler.Token.NumberToken;
import ch.njol.tome.compiler.Token.SingleLineCommentToken;
import ch.njol.tome.compiler.Token.StringToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WhitespaceToken;
import ch.njol.tome.parser.ParseError;
import ch.njol.tome.util.TokenListStream;

public class Lexer {
	
//	public final static Set<String> keywords = Collections.unmodifiableSet(new HashSet<>(Arrays.asList( //
//			"requires", "ensures", "invariant",//
//			"constructor", "delegate", //
//			"Self", "this", "new", "implies", "is", "arguments", "var", "null", "old", "recurse",)));
	
	private final SourceReader in;
	
	public Lexer(final SourceReader in) {
		this.in = in;
		tokenize();
	}
	
	public TokenListStream newStream() {
		return new TokenListStream(list());
	}
	
	public TokenList list() {
		return new TokenList(tokens);
	}
	
	private List<Token> tokens = new ArrayList<>();
	
	/**
	 * Performs an incremental update of the lexer.
	 * <p>
	 * Tokens before the start of the change remain unchanged, and following tokens may or may not change (e.g. if a new comment is started, everything afterwards becomes a comment
	 * usually). Tokens in the change will be removed and/or new ones will be added (a token is immutable, so a change is a remove and an insert).
	 * 
	 * @return The end of the changed region
	 */
	public int update(final int offset, final int length) {
		final TokenListStream oldTokens = newStream();
		tokens = new ArrayList<>(tokens.size() + 2); // usually only a single token or two are added, so the "+2" prevents an unnecessary resize operation
		Token ot = null, nt;
		
		// add unaffected tokens before the change to the new list
		while (oldTokens.getTextOffsetAfterCurrentToken() < offset && (ot = oldTokens.getAndMoveForward()) != null)
			tokens.add(ot);
		
		// set text position to start of first intersecting token
		in.setOffset(ot == null ? offset : oldTokens.getTextOffset());
		
		// ignore old tokens that intersect or touch the change
//		while ((ot = oldTokens.forward()) != null && ot.regionStart() <= offset + length + 1) {} // "+1" because tokenizing sometimes looks ahead a single character
		// 'ot' is now the first old token not intersecting the change
		
		// read new tokens
		while ((nt = next()) != null) {
			tokens.add(nt);
			
			// the following is not correct, as it wrongly thinks that a moved symbol for example is the same as an old one that was at that location before
			// so the only way to make this is to either know the change in position (which is probably not correct all the time...), or to parse tokens from the end (which is complicated as e.g. comments are left-to-right)
//			// move old token stream to new position
//			while (ot != null && ot.regionStart() < nt.regionStart())
//				ot = oldTokens.next();
//			// if a token is equal, all following ones must be equal too, so add them and stop.
//			if (ot != null && ot.equals(nt)) {
//				int end = ot.regionStart();
//				while ((ot = oldTokens.next()) != null)
//					tokens.add(ot);
//				return end;
//			}
		}
		return in.getOffset();
	}
	
	private void tokenize() {
		Token t;
		while ((t = next()) != null)
			tokens.add(t);
	}
	
	private boolean isInBlockCodeGen = false;
	
	private static boolean isLineEnd(final int x) {
		return x == '\n' || x == '\r';
	}
	
	private static boolean isLineOrFileEnd(final int x) {
		return x == '\n' || x == '\r' || x == -1;
	}
	
	// NOTE: do not look ahead more than 1 character (otherwise would need to change the incremental tokenizer)!
	private @Nullable Token next() {
		final int firstInt = in.next();
		if (firstInt == -1)
			return null;
		final char first = (char) firstInt;
		final int start = in.getOffset() - 1;
		if (Character.isLetter(first) || first == '_') { // word
			final StringBuilder b = new StringBuilder();
			b.append(first);
			int x;
			while ((x = in.peekNext()) != -1 && (Character.isLetterOrDigit((char) x) || x == '_'))
				b.append((char) in.next());
//			if (keywords.contains(b))
//				return new KeywordToken("" + b, start, in.getOffset());
			return Character.isUpperCase(b.charAt(0)) ? new UppercaseWordToken("" + b) : new LowercaseWordToken("" + b);
		} else if ('0' <= first && first <= '9') { // number (int or float)
			return parseNumber(first);
		} else if (first == '\'' || first == '"') { // string // TOOD LANG multi-line strings? (TBD: syntax, semantics) (e.g. 1 quote, 3 quotes, what about indentation, ...)
			return parseString(first);
		} else if (first == '#' && in.peekNext() != '|') { // single-line comment
			final StringBuilder b = new StringBuilder(Constants.SINGLE_LINE_COMMENT_START);
			while (!isLineOrFileEnd(in.peekNext()))
				b.append((char) in.next());
			return new SingleLineCommentToken("" + b);
		} else if (first == '#' && in.peekNext() == '|') { // multi-line comment
			in.next(); // skip '|'
			final StringBuilder b = new StringBuilder(Constants.MULTI_LINE_COMMENT_START);
			int prev = -1;
			int cur;
			int nesting = 0;
			while ((cur = in.peekNext()) != -1 && nesting >= 0) { // loop until EOF or end of comment
				if (prev == '#' && cur == '|') {
					nesting++;
					prev = -1; // prevent '#|#' behaving like '#||#'
				} else if (prev == '|' && cur == '#') {
					nesting--;
					prev = -1; // prevent '|#|' behaving like '|##|'
				} else {
					prev = cur;
				}
				b.append((char) in.next());
			}
			return new MultiLineCommentToken("" + b, nesting >= 0 ? Collections.singletonList(
					new ParseError("Missing " + (nesting == 0 ? "" : (nesting + 1) + " times ") + Constants.MULTI_LINE_COMMENT_END + " to end comment", start, b.length()))
					: Collections.emptyList());
		} else if (first == '$' && in.peekNext() != '=') { // code generation line or block (not call)
			// syntax for inserted expressions: $expr$
//			if (!isInBlockCodeGen && in.peekNext() == '{') {
//				isInBlockCodeGen = true;
//				in.next();
//			}
//			else if (isInBlockCodeGen && in.peekNext() == '}') {
//				isInBlockCodeGen = false;
//				in.next();
//				return new CodeGenerationToken("$}", true, start, in.getOffset());
//			}
			final StringBuilder b = new StringBuilder();
			boolean ended = false;
			int x;
			while ((x = in.next()) != -1 && x != '$' && (isInBlockCodeGen || !isLineEnd(x)))
				b.append((char) x);
			if (isInBlockCodeGen && in.peekNext() == '}') {
				isInBlockCodeGen = false;
				in.next();
				ended = true;
			} else if (!isInBlockCodeGen && isLineEnd(x)) {
				ended = true;
			}
			return new CodeGenerationToken(in.getText(start, in.getOffset()), "" + b, ended);
		} else if (Character.isWhitespace(first)) { // whitespace
			final StringBuilder b = new StringBuilder();
			b.append(first);
			int x;
			while ((x = in.peekNext()) != -1 && Character.isWhitespace((char) x))
				b.append((char) in.next());
			return new WhitespaceToken("" + b);
		} else { // symbol or unknown
			return new SymbolToken(first);
		}
	}
	
	private StringToken parseString(final char quote) {
		final int start = in.getOffset() - 1;
		final List<ParseError> errors = new ArrayList<>();
		final StringBuilder res = new StringBuilder();
		outer: while (true) {
			int c = in.next();
			if (c == -1 || isLineEnd(c)) {
				errors.add(new ParseError("Missing [" + quote + "] to end string", in.getOffset() - 1, 1));
				break;
			}
			if (quote == '\'') {
				if (c == '\'') {
					if (in.peekNext() != '\'') // end of single quoted string
						break;
					// else: escaped quote - skip one, so that only one ends up in the parsed string
					in.next();
				}
				res.append((char) c);
			} else {
				assert quote == '"';
				if (c == '"') // end of double quoted string
					break;
				if (c == '\\') {
					c = in.next();
					final String escaped = "0btnfr\\\"",
							replaces = "\0\b\t\n\f\r\\\"";
					final int e = escaped.indexOf(c);
					if (e >= 0) {
						res.append(replaces.charAt(e));
					} else if (c == 'u' || c == 'U') {
						final int unicodeStart = in.getOffset() - 2;
						int value = 0;
						for (int i = 0; i < (c == 'u' ? 4 : 6); i++) {
							final int d = in.next();
							if (d == -1 || isLineEnd(d)) {
								errors.add(new ParseError("Missing [" + quote + "] to end string", in.getOffset(), 0));
								break;
							}
							final int dv = charToInt((char) d, 16);
							if (dv >= 0) {
								value = 16 * value + dv;
							} else {
								errors.add(new ParseError("Unicode character escapes must be of the form '\\uXXXX' or '\\UXXXXXX', where each X is a hexadecimal digit.", unicodeStart, in.getOffset() - unicodeStart));
								continue outer;
							}
						}
						try {
							res.appendCodePoint(value);
						} catch (final IllegalArgumentException ex) {
							errors.add(new ParseError("Invalid unicode code point " + Integer.toHexString(value), unicodeStart, in.getOffset() - unicodeStart));
						}
					} else {
						if (c == -1 || isLineEnd(c)) {
							errors.add(new ParseError("Missing [" + quote + "] to end string", in.getOffset(), 0));
							break;
						}
						errors.add(new ParseError("Invalid escape sequence [\\" + (char) c + "]", in.getOffset() - 2, 2));
					}
				} else {
					res.append((char) c);
				}
			}
		}
		return new StringToken(in.getText(start, in.getOffset()), "" + res, errors);
	}
	
	private final static int charToInt(final char c, final int radix) {
		assert 0 < radix && radix <= 36;
		if ('0' <= c && c <= ('0' + Math.min(radix - 1, 9)))
			return c - '0';
		if ('a' <= c && c <= ('a' + radix - 10))
			return c - 'a' + 10;
		if ('A' <= c && c <= ('A' + radix - 10))
			return c - 'A' + 10;
		return -1;
	}
	
	private final BigDecimal oneThenth = new BigDecimal("0.1"); // not the double 0.1, as that is not exact
	private final BigDecimal oneHalf = new BigDecimal(0.5);
	private final BigDecimal oneSixteenth = new BigDecimal(1.0 / 16);
	
	private NumberToken parseNumber(int first) {
		int radix = 10;
		final int start = in.getOffset() - 1;
		if (first == '0') {
			final int second = in.peekNext();
			if (second == 'x') {
				in.next(); // skip 'x'
				do {
					first = in.next();
				} while (first == '_');
				radix = 16;
			} else if (second == 'b') {
				in.next(); // skip 'b'
				do {
					first = in.next();
				} while (first == '_');
				radix = 2;
			}
		}
		final BigDecimal bigRadix = new BigDecimal(radix);
		final int fd = charToInt((char) first, radix);
		if (fd == -1)
			return new NumberToken(in.getText(start, in.getOffset()), new BigDecimal(0), Arrays.asList(new ParseError("Missing digit after " + (radix == 16 ? "'0x' (start of a hexadecimal number)" : "'0b' (start of a binary number)"), start, in.getOffset() - start)));
		BigDecimal res = new BigDecimal(fd);
		int numIntDigits = 1;
		int c = first;
		while (true) {
			c = in.peekNext();
			if (c == '_') {
				in.next();
				continue;
			}
			final int d = charToInt((char) c, radix);
			if (d == -1)
				break;
			res = res.multiply(bigRadix).add(new BigDecimal(d));
			in.next();
			numIntDigits++;
		}
		BigDecimal fract = new BigDecimal(1);
		if (in.peekNext() == '.') {
			in.next(); // skip period
			while (true) {
				c = in.next();
				if (c == -1)
					break;
				if (c == '_')
					continue;
				final int d = charToInt((char) c, radix);
				if (d == -1) {
					in.back();
					break;
				}
				fract = fract.multiply(radix == 2 ? oneHalf : radix == 16 ? oneSixteenth : oneThenth);
				res = res.add(new BigDecimal(d).multiply(fract));
			}
			if (in.peekNext(-1) == '.') // no digits after the period
				in.back();
		}
		assert res != null;
		String error = null;
		if (radix == 2 && numIntDigits % 4 != 0)
			error = "Warning: length of this binary number literal is not a multiple of 4";
//		else if (radix == 16 && numIntDigits % 2 != 0)
//			error = "Warning: length of this hexadecimal number literal is not a multiple of 2";
		return new NumberToken(in.getText(start, in.getOffset()), res, error == null ? Collections.EMPTY_LIST : Arrays.asList(new ParseError(error, start, in.getOffset() - start)));
	}
	
}
