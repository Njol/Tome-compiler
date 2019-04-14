package ch.njol.tome.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTDocument;
import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.SimpleASTDocument;
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

public class Parser {
	
	protected final TokenListStream in;
	
	protected final List<ASTElementPart> parts = new ArrayList<>();
	
	protected @Nullable Parser parent;
	
	protected @Nullable Parser currentChild;
	
	protected boolean valid = true;
	
	public Parser(final TokenListStream in) {
		this.in = in;
		parent = null;
	}
	
	protected Parser(final Parser parent) {
		in = parent.in;
		this.parent = parent;
	}
	
	@SuppressWarnings("null")
	private void assertNoCurrentChild() {
		assert currentChild == null : currentChild.parts.stream().map(e -> e + "[" + e.getClass() + "]").collect(Collectors.toList());
	}
	
	public Parser start() {
		assert valid;
		assertNoCurrentChild();
		return currentChild = new Parser(this);
	}
	
	/**
	 * Creates a new, empty parent of the current element.
	 */
	public Parser startNewParent() {
		assert valid;
		if (parent != null)
			parent.currentChild = null;
		final Parser newParent = parent != null ? parent.start() : new Parser(in);
		newParent.currentChild = this;
		parent = newParent;
		return newParent;
	}
	
	protected void addPart(final ASTElementPart part) {
		assert valid;
		assertNoCurrentChild();
		parts.add(part);
		if (part instanceof Token)
			fatalParseErrors.addAll(((Token) part).errors());
	}
	
	private void childDone(final Parser child) {
		assert valid;
		assert currentChild == child;
		fatalParseErrors.addAll(child.fatalParseErrors);
		possibleParseErrors.clear();
		currentChild = null;
	}
	
	public <T extends ASTElement> T done(final T ast) {
		assert valid;
		assertNoCurrentChild();
		final Parser parent = this.parent;
		assert parent != null;
		final List<ASTElementPart> parentParts = parent.parts;
		final List<WhitespaceOrCommentToken> endingWhitespace = removeEndingWhitespace(parts);
		ast.addChildren(parts);
		parentParts.add(ast);
		parentParts.addAll(endingWhitespace);
		parent.childDone(this);
		valid = false;
		return ast;
	}
	
	public void doneAsChildren() {
		assert valid;
		assert currentChild == null;
		final Parser parent = this.parent;
		assert parent != null;
		parent.parts.addAll(parts);
		parent.childDone(this);
		valid = false;
	}
	
	private static List<WhitespaceOrCommentToken> removeEndingWhitespace(final List<ASTElementPart> parts) {
		final ArrayList<WhitespaceOrCommentToken> result = new ArrayList<>();
		for (int i = parts.size() - 1; i >= 0; i--) {
			final ASTElementPart p = parts.get(i);
			if (!(p instanceof WhitespaceOrCommentToken))
				break;
			result.add(0, (WhitespaceOrCommentToken) p);
			parts.remove(i);
		}
		return result;
	}
	
	public <T extends ASTElement> ASTDocument<T> documentDone(final T ast) {
		assert parent == null;
		assert valid;
		assert currentChild == null;
		assert ast == parts.get(0);
		final List<WhitespaceOrCommentToken> endingWhitespace = removeEndingWhitespace(parts);
		assert parts.size() == 1;
		ast.addChildren(endingWhitespace);
		
		if (!in.isAfterEnd())
			errorFatal("Unexpected data at end of document (or unable to parse due to previous errors)", in.getTextOffset(), in.getTextLength() - in.getTextOffset());
		Token t;
		while ((t = in.getAndMoveForward()) != null) {
			ast.addChild(t);
		}
		
		valid = false;
		return new SimpleASTDocument<>(ast, fatalParseErrors);
	}
	
	public void cancel() {
		assert valid;
		assert parts.isEmpty();
		assert currentChild == null;
		final Parser parent = this.parent;
		assert parent != null;
		assert parent.valid;
		parent.currentChild = null;
		valid = false;
	}
	
	public <T extends ASTElement> T one(final Function<Parser, T> p) {
		final Parser child = start();
		final T result = p.apply(child);
		assert valid;
		assert currentChild == child;
		child.done(result);
		return result;
	}
	
	/**
	 * Removes a parsed element, and replaces it with its children
	 * 
	 * @param ast
	 */
	public void unparse(final ASTElement ast) {
		assert valid;
		assert currentChild == null;
		assert ast.parent() == null;
		int index = -1;
		for (int i = 0; i < parts.size(); i++) {
			if (parts.get(i) == ast) {
				index = i;
				break;
			}
		}
		assert index >= 0;
		parts.remove(index);
		int i = index;
		for (final ASTElementPart part : ast.parts()) {
			parts.add(i, part);
			i++;
		}
		ast.clearChildren();
		ast.invalidateSelf();
		return;
	}
	
	private final List<ParseError> fatalParseErrors = new ArrayList<>(),
			possibleParseErrors = new ArrayList<>();
	
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
	
	private static int lastGuard = -1;
	private static final Deque<Character> guards = new ArrayDeque<>();
	
	private static final void startGuard(final char guard) {
		guards.addLast(guard);
		lastGuard = guard;
	}
	
	private static final String openingBrackets = "([{<", closingBrackets = ")]}>";
	
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
				addPart(t);
			}
			if (t != null) {
				// TODO required? or only if no errors so far? or make a different error?
				errorFatal("Unexpected data before '" + guard + "'", start, in.getTextOffset() - start);
				if (consume) {
					addPart(t);
					in.skipWhitespace(this::addPart);
				} else {
					in.moveBackward();
				}
			} else {
				errorFatal(ParseError.EXPECTED + "'" + guard + "'", start, 1);
				assert in.isAfterEnd();
			}
		} else {
			success = true;
			if (consume) {
				addPart(next);
				in.moveForward();
				in.skipWhitespace(this::addPart);
			}
		}
		oneFromTry(success);
	}
	
	public final @Nullable Token next() {
		final Token t = in.current();
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		if (t != null)
			addPart(t);
		in.moveForward();
		in.skipWhitespace(this::addPart);
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
	
	public final boolean peekNext(final String wordOrSymbol) {
		final boolean success = peekNext(wordOrSymbol, 0, false);
		if (!success)
			expectedPossible("'" + wordOrSymbol + "'");
		return success;
	}
	
	// FIXME doesn't "expectPossible"
	public final boolean peekNext(final String wordOrSymbol, final int delta, final boolean skipWhitespace) {
		if (Character.isLetter(wordOrSymbol.codePointAt(0))) {
			final Token t = in.peekNext(delta, skipWhitespace);
			return t instanceof WordToken && ((WordToken) t).word.equals(wordOrSymbol);
		} else if (wordOrSymbol.length() == 1) {
			final Token t = in.peekNext(delta, skipWhitespace);
			return t instanceof SymbolToken && ((SymbolToken) t).symbol == wordOrSymbol.charAt(0);
		} else {
			final TokenListStream inClone = in.clone();
			for (int i = 0; i < delta; i++) {
				inClone.moveForward();
				if (skipWhitespace)
					inClone.skipWhitespace(c -> {});
			}
			for (int i = 0; i < wordOrSymbol.length(); i++) {
				final Token t = inClone.getAndMoveForward();
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
		lambda.process();
		endGuard(until, true);
	}
	
	public void repeatUntil(final VoidProcessor lambda, final char until, final boolean allowEmpty) {
		repeatUntil(lambda, until, allowEmpty, true);
	}
	
	public void repeatUntil(final VoidProcessor lambda, final char until, final boolean allowEmpty, final boolean consumeEndChar) {
		if (allowEmpty && (consumeEndChar ? try_(until) : peekNext(until))) {
			return;
		}
		startGuard(until);
		do {
			final int s = in.getTokenOffset();
			lambda.process();
			if (in.getTokenOffset() == s)
				break;
		} while (peekNext() != null);
		endGuard(until, consumeEndChar);
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
		SymbolToken startToken = one(start);
		if (startToken == null)
			return;
		until(lambda, end, true);
	}
	
	public void oneRepeatingGroup(final char start, final VoidProcessor lambda, final char end) {
		SymbolToken startToken = one(start);
		if (startToken == null)
			return;
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
		if (t instanceof SymbolToken && ((SymbolToken) t).symbol == symbol && symbol != lastGuard) {
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
			if (keywordOrSymbol.length() == 1)
				return try2(keywordOrSymbol.charAt(0));
			final List<SymbolToken> tokens = new ArrayList<>();
			for (int i = 0; i < keywordOrSymbol.length(); i++) {
				final Token t = peekNext(i, false); // not in.peekNext to handle guards correctly
				if (!(t instanceof SymbolToken && ((SymbolToken) t).symbol == keywordOrSymbol.charAt(i))) {
					expectedPossible("'" + keywordOrSymbol + "'");
					return null;
				}
				tokens.add((SymbolToken) t);
			}
			// do not add Tokens to current parts - SymbolsWord takes care of handling its own AST
			for (int i = 0; i < keywordOrSymbol.length(); i++)
				in.moveForward();
			trySuccessful();
			final SymbolsWord sw = new SymbolsWord(tokens);
			addPart(sw);
			in.skipWhitespace(this::addPart);
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
			if (try_(s))
				return s;
		}
		return null;
	}
	
	public final @Nullable WordOrSymbols try2(final String... symbolsAndWords) {
		for (final String s : symbolsAndWords) {
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
