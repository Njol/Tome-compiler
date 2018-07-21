package ch.njol.tome.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.SymbolsWord;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.util.TokenListStream;
import ch.njol.util.CollectionUtils;

public abstract class Parser implements ASTParser {
	
	protected final TokenListStream in;
	
	public Parser(final TokenListStream in) {
		this.in = in;
	}
	
	protected @Nullable AttachedElementParser<?> currentChild = null;
	
	protected abstract void addWhitespace(final WhitespaceOrCommentToken t);
	
	protected abstract void addWhitespace(final List<WhitespaceOrCommentToken> tokens);
	
	public final <C extends ASTElement> ElementPlaceholder<C> startChild() {
		return new ElementPlaceholder<>(this);
	}

	/**
	 * @return A new {@link UnknownParser}. Can later be replaced with an element using its {@link UnknownParser#toElement(ASTElement) toElement}
	 * 		method, or be completely ignored.
	 */
	public final UnknownParser startUnknownChild() {
		return new UnknownParser(this);
	}
	
//	public ProtoElementParser
	
	private final List<ParseError> fatalParseErrors = new ArrayList<>(),
			possibleParseErrors = new ArrayList<>();
	
	@Override
	public List<ParseError> fatalParseErrors() {
		return fatalParseErrors;
	}
	
	public void expectedFatal(final String expected) {
		fatalParseErrors.add(new ParseError(ParseError.EXPECTED + expected, in.getTextOffset(), 1));
	}
	
	public void expectedFatal(final String expected, @Nullable final Token instead) {
		if (instead == null) {
			expectedFatal(expected);
			return;
		}
		fatalParseErrors.add(new ParseError(ParseError.EXPECTED + expected, instead.absoluteRegionStart(), instead.regionLength()));
	}
	
	public void expectedPossible(final String expected) {
		if (!fatalParseErrors.isEmpty())
			return;
		possibleParseErrors.add(new ParseError(ParseError.EXPECTED + expected, in.getTextOffset(), 1));
	}
	
	public void errorFatal(final String error, final int start, final int length) {
		fatalParseErrors.add(new ParseError(error, start, length));
	}
	
	// ---------------------------------------- general ----------------------------------------
	
	// TODO allow guards that block even if they are not the current one? (cannot do this in general or nested brackets wouldn't work, e.g. '(Type<(T)>)')
	
	private int lastGuard = -1;
	private final Deque<Character> guards = new ArrayDeque<>();
	
	private final void startGuard(final char guard) {
		guards.addLast(guard);
		lastGuard = guard;
	}
	
	private final static String openingBrackets = "([{<", closingBrackets = ")]}>";
	
	private final void endGuard(final char guard, final boolean consume) {
		final char last = guards.removeLast();
		assert last == guard;
		assert last == lastGuard;
		lastGuard = guards.isEmpty() ? -1 : guards.getLast();
		final int opening = closingBrackets.indexOf(guard) < 0 ? -1 : openingBrackets.charAt(closingBrackets.indexOf(guard));
		boolean success;
		final Token next = in.current();
		if (!(next instanceof SymbolToken && ((SymbolToken) next).symbol == guard)) {
			success = false;
			final int start = in.getTextOffset();
			int groups = 0;
			Token t;
			while (true) {
				t = in.getAndMoveForward();
				if (t == null)
					break;
				if (t instanceof SymbolToken) {
					if (((SymbolToken) t).symbol == opening)
						groups++;
					else if (((SymbolToken) t).symbol == guard)
						groups--;
					if (groups < 0)
						break;
				}
				addChildToAST(t);
			}
			if (t != null) {
				if (consume) {
					addChildToAST(t);
					in.skipWhitespace(this::addWhitespace);
				} else {
					in.moveBackward();
				}
				// TODO required? or only if no errors so far? or make a different error?
				errorFatal("Unexpected data before '" + guard + "'", start, in.getTextOffset() - start);
			} else {
				errorFatal(ParseError.EXPECTED + "'" + guard + "'", start, 1);
				assert in.isAfterEnd();
			}
		} else {
			success = true;
			if (consume) {
				addChildToAST(next);
				in.moveForward();
				in.skipWhitespace(this::addWhitespace);
			}
		}
		oneFromTry(success);
	}
	
	public final @Nullable Token next() {
		final Token t = in.current();
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		if (t != null)
			addChildToAST(t);
		in.moveForward();
		in.skipWhitespace(this::addWhitespace);
		return t;
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends Token> @Nullable T next(final Class<T> type) {
		final Token t = next();
		return type.isInstance(t) ? (T) t : null;
	}
	
	/**
	 * <big><b>Ruins content assist!</b></big>
	 * 
	 * @return
	 */
	public final @Nullable Token peekNext() {
		final Token t = in.current();
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		return t;
	}
	
	/**
	 * <big><b>Ruins content assist!</b></big>
	 * 
	 * @return
	 */
	public final @Nullable Token peekNext(final int delta, final boolean skipWhitespace) {
		final Token t = in.peekNext(delta, skipWhitespace);
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		return t;
	}
	
	public final boolean peekNext(final char symbol) {
		final boolean success = peekNext(symbol, 0, false);
		if (!success)
			expectedPossible("'" + symbol + "'");
		return success;
	}
	
	public final boolean peekNext(final char symbol, final int delta, final boolean skipWhitespace) {
		final Token t = in.peekNext(delta, skipWhitespace);
		return t instanceof SymbolToken && ((SymbolToken) t).symbol == symbol;
	}
	
	public final boolean peekNext(final String word) {
		final boolean success = peekNext(word, 0, false);
		if (!success)
			expectedPossible("'" + word + "'");
		return success;
	}
	
	// FIXME doesn't "expectPossible"
	public final boolean peekNext(final String wordOrSymbol, final int delta, final boolean skipWhitespace) {
		if (Character.isLetter(wordOrSymbol.codePointAt(0))) {
			final Token t = in.peekNext(delta, skipWhitespace);
			return t instanceof WordToken && ((WordToken) t).word.equals(wordOrSymbol);
		} else {
			Token t = in.peekNext(delta, skipWhitespace);
			if (t == null)
				return false;
			final TokenListStream inClone = in.clone();
			inClone.setTokenOffset(t);
			for (int i = 0; i < wordOrSymbol.length(); i++) {
				t = inClone.getAndMoveForward();
				if (!(t instanceof SymbolToken && ((SymbolToken) t).symbol == wordOrSymbol.charAt(i)))
					return false;
			}
			return true;
		}
	}
	
	public final boolean peekNextOneOf(final char... symbols) {
		final Token t = peekNext();
		if (t instanceof SymbolToken && CollectionUtils.contains(symbols, ((SymbolToken) t).symbol))
			return true;
		for (final char symbol : symbols)
			expectedPossible("'" + symbol + "'");
		return false;
	}
	
	public final boolean peekNextOneOf(final int delta, final boolean skipWhitespace, final char... symbols) {
		final Token t = peekNext(delta, skipWhitespace);
		if (t instanceof SymbolToken && CollectionUtils.contains(symbols, ((SymbolToken) t).symbol))
			return true;
		for (final char symbol : symbols)
			expectedPossible("'" + symbol + "'");
		return false;
	}
	
	public final boolean peekNextOneOf(final String... wordsOrSymbols) {
		for (final String wordOrSymbol : wordsOrSymbols) {
			if (peekNext(wordOrSymbol))
				return true;
		}
		return false;
	}
	
	// ---------------------------------------- control flow ----------------------------------------
	
	public static interface VoidProcessor {
		void process();
	}
	
	// TODO optimise if nothing matched? (how?)
	public void until(final VoidProcessor lambda, final char until, final boolean allowEmpty) {
		if (allowEmpty && try_(until)) {
//					if (allowEmpty)
//					else
//						expectedFatal("what?", start, length);
			return;
		}
		startGuard(until);
		try {
			lambda.process();
		} finally {
			endGuard(until, true);
		}
	}
	
	public void repeatUntil(final VoidProcessor lambda, final char until, final boolean allowEmpty) {
		repeatUntil(lambda, until, allowEmpty, true);
	}
	
	public void repeatUntil(final VoidProcessor lambda, final char until, final boolean allowEmpty, final boolean consumeEndChar) {
		if (allowEmpty && try_(until)) {
			return;
		}
		startGuard(until);
		try {
			do {
				final int s = in.getTokenOffset();
				lambda.process();
				if (in.getTokenOffset() == s)
					break;
			} while (peekNext() != null);
		} finally {
			endGuard(until, consumeEndChar);
		}
	}
	
	public void repeatUntilEnd(final VoidProcessor lambda) {
		while (peekNext() != null) {
			final int s = in.getTokenOffset();
			lambda.process();
			if (in.getTokenOffset() == s)
				break;
		}
		if (!in.isAfterEnd()) {
			errorFatal("Unexpected data at end of document", in.getTextOffset(), in.getTextLength() - in.getTextOffset());
		}
	}
	
	public void untilEnd(final VoidProcessor lambda) {
		lambda.process();
		if (!in.isAfterEnd()) {
			errorFatal("Unexpected data at end of document", in.getTextOffset(), in.getTextLength() - in.getTextOffset());
		}
	}
	
	public void oneGroup(final char start, final VoidProcessor lambda, final char end) {
		one(start);
		until(lambda, end, true);
	}
	
	public void oneRepeatingGroup(final char start, final VoidProcessor lambda, final char end) {
		one(start);
		repeatUntil(lambda, end, true);
	}
	
	public boolean tryGroup(final char start, final VoidProcessor lambda, final char end) {
		if (try_(start)) {
			until(lambda, end, true);
			return true;
		}
		return false;
	}
	
	public boolean tryRepeatingGroup(final char start, final VoidProcessor lambda, final char end) {
		if (try_(start)) {
			repeatUntil(lambda, end, true);
			return true;
		}
		return false;
	}
	
	/**
	 * Parses several blocks in an unordered fashion, and ignores missing ones. make sure the blocks don't consume any tokens if they have not been parsed (i.e. use
	 * {@link #try_(String)} or similar)
	 */
	public void unordered(final @NonNull VoidProcessor... lambdas) {
		final List<VoidProcessor> notDone = new ArrayList<>(Arrays.asList(lambdas));
		outer: while (true) {
			final int start = in.getTokenOffset();
			for (int i = 0; i < notDone.size(); i++) {
				notDone.get(i).process();
				if (in.getTokenOffset() > start) {
					notDone.remove(i);
					continue outer;
				}
			}
			return;
		}
	}
	
	// ---------------------------------------- one ----------------------------------------
	
	// call like 'oneFromTry(try_(whatever))'
	private final void oneFromTry(final boolean success) {
		if (!success)
			fatalParseErrors.addAll(possibleParseErrors);
		possibleParseErrors.clear();
	}
	
	private final <T> @Nullable T oneFromTry(final @Nullable T t) {
		oneFromTry(t != null);// && (!(t instanceof Element) || (((Element) t).fatalParseErrors().isEmpty() || ((Element) t).isParseUnique())));
		return t;
	}
	
//			private final <T extends Token> @Nullable T oneFromTry(final @Nullable T token) {
//				oneFromTry(token != null);
//				return token;
//			}

	public final @Nullable SymbolToken one(final char symbol) {
		return oneFromTry(try2(symbol));
	}
	
	public final @Nullable WordOrSymbols one(final String keywordOrSymbol) {
		if (Character.isLetter(keywordOrSymbol.charAt(0)))
			return oneFromTry(try2(keywordOrSymbol));
		oneFromTry(try_(keywordOrSymbol));
		return null;
	}
	
	public final @Nullable String oneOf(final String... keywordsAndSymbols) {
//				int start = in.getOffset();
		for (final String s : keywordsAndSymbols) {
			assert s != null;
			if (try_(s))
				return oneFromTry(s);
		}
//				expectedFatal("one of "+Arrays.toString(symbolsAndWords), start, 1);
		return oneFromTry((String) null);
	}
	
//	public final <T extends ASTElement> T one(final Function<Parser, Parsed<T>> parseFunction) {
////				final int start = in.getTokenOffset();
//		final T r = parseFunction.apply(parser).ast;
//		// just duplicates errors usually (TODO check)
//		// could just add non-duplicates to fix this...
////				if (in.getTokenOffset() > start) {
//		// an 'until' can match everything but not result in any tokens added to the element
////					assert r.regionLength() > 0 : r + " / " + t + " : " + t.regionLength() + " // " + r.fatalParseErrors() + " :: " + start + " / " + in;
//		addChildToAST(r);
////				}
//		fatalParseErrors.addAll(r.fatalParseErrors());
////				assert !r.fatalParseErrors().isEmpty() || r != t || in.getTokenOffset() > start : t + " / " + in + " :: " + ((AbstractElement<?>) t).possibleParseErrors;
//		return oneFromTry(r);
//	}
	
	public final @Nullable String oneVariableIdentifier() {
		return oneFromTry(tryVariableIdentifier());
	}
	
	public final @Nullable LowercaseWordToken oneVariableIdentifierToken() {
		return oneFromTry(tryVariableIdentifierToken());
	}
	
	public final @Nullable String oneTypeIdentifier() {
		return oneFromTry(tryTypeIdentifier());
	}
	
	public final @Nullable UppercaseWordToken oneTypeIdentifierToken() {
		return oneFromTry(tryTypeIdentifierToken());
	}
	
	public final @Nullable WordToken oneIdentifierToken() {
		return oneFromTry(tryIdentifierToken());
	}
	
	// ---------------------------------------- try ----------------------------------------
	
	private void trySuccessful() {
		oneFromTry(true);
	}
	
	public final @Nullable SymbolToken try2(final char symbol) {
		final Token t = in.current();
		if (t instanceof SymbolToken && ((SymbolToken) t).symbol == symbol) {
			next();
			trySuccessful();
			return (SymbolToken) t;
		}
		expectedPossible("'" + symbol + "'");
		return null;
	}
	
	public final boolean try_(final char symbol) {
		return try2(symbol) != null;
	}
	
	public final @Nullable WordOrSymbols try2(final String keywordOrSymbol) {
		if (Character.isLetter(keywordOrSymbol.charAt(0))) {
			final Token t = in.current();
			if (t instanceof WordToken && ((WordToken) t).word.equals(keywordOrSymbol)) {
				next();
				((WordToken) t).keyword = true;
				trySuccessful();
				return (WordToken) t;
			}
		} else {
			final List<SymbolToken> tokens = new ArrayList<>();
			for (int i = 0; i < keywordOrSymbol.length(); i++) {
				final Token t = in.peekNext(i, false);
				if (!(t instanceof SymbolToken && ((SymbolToken) t).symbol == keywordOrSymbol.charAt(i))) {
					expectedPossible("'" + keywordOrSymbol + "'");
					return null;
				}
				tokens.add((SymbolToken) t);
			}
			for (int i = 0; i < keywordOrSymbol.length(); i++)
				next();
			trySuccessful();
			if (tokens.size() == 1)
				return tokens.get(0);
			final SymbolsWord sw = new SymbolsWord(tokens);
			addChildToAST(sw);
			return sw;
		}
		expectedPossible("'" + keywordOrSymbol + "'");
		return null;
	}
	
	public final boolean try_(final String keywordOrSymbol) {
		return try2(keywordOrSymbol) != null;
	}
	
	public final @Nullable String tryVariableIdentifier() {
		final LowercaseWordToken t = tryVariableIdentifierToken();
		return t == null ? null : t.word;
	}
	
	public final @Nullable LowercaseWordToken tryVariableIdentifierToken() {
		final Token t = peekNext();
		if (t instanceof LowercaseWordToken) {
			next();
			((WordToken) t).keyword = false;
			return (LowercaseWordToken) t;
		}
		expectedPossible("a non-type identifier");
		return null;
	}
	
	public final @Nullable String tryTypeIdentifier() {
		final UppercaseWordToken t = tryTypeIdentifierToken();
		return t == null ? null : t.word;
	}
	
	public final @Nullable UppercaseWordToken tryTypeIdentifierToken() {
		final Token t = peekNext();
		if (t instanceof UppercaseWordToken) {
			next();
			((WordToken) t).keyword = false;
			return (UppercaseWordToken) t;
		}
		expectedPossible("a type identifier");
		return null;
	}
	
	public final @Nullable WordToken tryIdentifierToken() {
		final Token t = peekNext();
		if (t instanceof WordToken) {
			next();
			((WordToken) t).keyword = false;
			return (WordToken) t;
		}
		expectedPossible("an identifier");
		return null;
	}
	
	public final @Nullable String try_(final String... symbolsAndWords) {
		for (final String s : symbolsAndWords) {
			assert s != null;
			if (try_(s))
				return s;
		}
		return null;
	}
	
	public final @Nullable WordOrSymbols try2(final String... symbolsAndWords) {
		for (final String s : symbolsAndWords) {
			assert s != null;
			final WordOrSymbols t = try2(s);
			if (t != null)
				return t;
		}
		return null;
	}
	
	public final @Nullable SymbolToken try2(final char... symbols) {
		for (final char c : symbols) {
			final SymbolToken t = try2(c);
			if (t != null)
				return t;
		}
		return null;
	}
	
}
