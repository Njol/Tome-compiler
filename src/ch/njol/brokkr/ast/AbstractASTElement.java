package ch.njol.brokkr.ast;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.compiler.ParseError;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.SymbolToken;
import ch.njol.brokkr.compiler.Token.SymbolsWordToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.TokenStream;
import ch.njol.brokkr.interpreter.Interpreter;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

public abstract class AbstractASTElement<E extends ASTElement> implements ASTElement {
	
	public @Nullable ASTElement parent = null;
	
	@Override
	public void setParent(final @Nullable ASTElement parent) {
		assert parent != this;
		if (this.parent != parent) {
			if (this.parent != null)
				this.parent.parts().remove(this);
			if (parent != null)
				parent.parts().add(this);
			this.parent = parent;
		}
	}
	
	@Override
	public @Nullable ASTElement parent() {
		return parent;
	}
	
	protected final @Nullable IRTypeDefinition getStandardType(final String module, final String type) {
		return ASTBrokkrFile.getStandardType(this, module, type);
	}
	
	protected final Interpreter getInterpreter() {
		final Modules mods = getModules();
		if (mods == null)
			throw new InterpreterException("missing module");
		return mods.interpreter;
	}
	
	protected final @Nullable Modules getModules() {
		final ASTBrokkrFile file = getParentOfType(ASTBrokkrFile.class);
		if (file == null)
			return null;
		return file.modules;
	}
	
	protected final @Nullable Module getModule() {
		final ASTBrokkrFile file = getParentOfType(ASTBrokkrFile.class);
		if (file == null)
			return null;
		return file.module;
	}
	
	@Override
	public int regionStart() {
		if (parts.isEmpty())
			return -1;
		return parts.get(0).regionStart();
	}
	
	@Override
	public int regionEnd() {
		if (parts.isEmpty())
			return -1;
		return parts.get(parts.size() - 1).regionEnd();
	}
	
	private final List<ASTElementPart> parts = new ArrayList<>();
	
	@Override
	public List<ASTElementPart> parts() {
		return parts;
	}
	
	@Override
	public void addLink(final ASTLink<?> link) {
		links.add(link);
	}
	
	private final List<ASTLink<?>> links = new ArrayList<>();
	
	@Override
	public List<ASTLink<?>> links() {
		return links;
	}
	
//		public @Nullable Symbol lastSymbol() {
//			for (int i = parts.size() - 1; i >= 0; i--) {
//				if (parts.get(i) instanceof Symbol)
//					return (Symbol) parts.get(i);
//			}
//			return null;
//		}
	
	@Override
	public void print(final PrintStream out, String indent) {
		out.print("/*" + getClass().getSimpleName() + "*/ ");
		for (final ASTElementPart part : parts()) {
			if (part instanceof ASTElement) {
				part.print(out, indent);
			} else {
				final String s = part.toString();
				if (s.equals("{") || s.equals("}") || s.equals(";")) {
					if (s.equals("{"))
						indent += "   ";
					else if (s.equals("}"))
						indent = "" + indent.substring(0, indent.length() - 3);
					out.println(s);
					out.print(indent);
				} else if (s.equals("=>")) {
					out.println();
					out.print(indent + "   " + s + " ");
				} else {
					out.print(s);
					out.print(' ');
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "" + getClass().getSimpleName();
	}
	
	@SuppressWarnings("null")
	private TokenStream in = null;
	
	@SuppressWarnings("null")
	public E parse(final TokenStream in) throws ParseException {
		this.in = in;
		try {
			return parse();
		} finally {
			this.in = null;
		}
	}
	
	protected abstract E parse() throws ParseException;
	
	private final List<ParseError> fatalParseErrors = new ArrayList<>(), possibleParseErrors = new ArrayList<>();
	
	@Override
	public List<ParseError> fatalParseErrors() {
		return fatalParseErrors;
	}
	
	public void compactFatalParseErrors() {
		final List<ParseError> errors = new ArrayList<>();
		final Map<Integer, Set<String>> expected = new HashMap<>();
		for (final ParseError e : fatalParseErrors) {
			if (e.message.startsWith(ParseError.EXPECTED)) {
				Set<String> s = expected.get(e.start);
				if (s == null)
					expected.put(e.start, s = new HashSet<>());
				s.add(e.message.substring(ParseError.EXPECTED.length()));
			} else {
				errors.add(e);
			}
		}
		fatalParseErrors.clear();
		fatalParseErrors.addAll(errors);
		for (final Entry<Integer, Set<String>> e : expected.entrySet()) {
			String message = ParseError.EXPECTED;
			final List<String> es = new ArrayList<>(e.getValue());
			Collections.sort(es);
			for (int i = 0; i < es.size(); i++) {
				if (i != 0 && i == es.size() - 1)
					message += ", or ";
				else if (i != 0)
					message += ", ";
				message += es.get(i);
			}
			fatalParseErrors.add(new ParseError(message, e.getKey(), 1));
		}
	}
	
	protected void expectedFatal(final String expected) {
		fatalParseErrors.add(new ParseError(ParseError.EXPECTED + expected, in.getTextOffset(), 1));
	}
	
	protected void expectedFatal(final String expected, @Nullable final Token instead) {
		if (instead == null) {
			expectedFatal(expected);
			return;
		}
		fatalParseErrors.add(new ParseError(ParseError.EXPECTED + expected, instead.regionStart(), instead.regionLength()));
	}
	
	protected void expectedPossible(final String expected) {
		if (!fatalParseErrors.isEmpty())
			return;
		possibleParseErrors.add(new ParseError(ParseError.EXPECTED + expected, in.getTextOffset(), 1));
	}
	
	protected void errorFatal(final String error, final int start, final int length) {
		fatalParseErrors.add(new ParseError(error, start, length));
	}
	
	// ---------------------------------------- general ----------------------------------------
	
	// TODO allow guards that block even if not the last one? (cannot do this in general or nested brackets wouldn't work, e.g. '(Type<(T)>)')
	
	private int lastGuard = -1;
	private final Deque<Character> guards = new ArrayDeque<>();
	
	private final void startGuard(final char guard) {
		guards.addLast(guard);
		lastGuard = guard;
	}
	
	private final static String openingBrackets = "([{<", closingBrackets = ")]}>";
	
	private final void endGuard(final char guard) {
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
				t = in.forward();
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
			}
			if (t != null) {
				t.setParent(this);
				in.skipWhitespace();
				// TODO required? or only if no errors so far? or make a different error?
				errorFatal("Unexpected data before '" + guard + "'", start, t.regionStart() - start);
			} else {
				errorFatal(ParseError.EXPECTED + "'" + guard + "'", start, 1);
				assert in.isAfterEnd();
			}
		} else {
			success = true;
			next.setParent(this);
			in.forward();
			in.skipWhitespace();
		}
		// same as in oneFromTry, but without throwing an exception
		oneFromTryNoException(success);
	}
	
	protected final @Nullable Token next() {
		final Token t = in.current();
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		in.forward();
		if (t != null)
			t.setParent(this);
		in.skipWhitespace();
		return t;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends Token> @Nullable T next(final Class<T> type) {
		final Token t = next();
		return type.isInstance(t) ? (T) t : null;
	}
	
	/**
	 * <big><b>Ruins content assist!</b></big>
	 * 
	 * @return
	 */
	protected final @Nullable Token peekNext() {
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
	protected final @Nullable Token peekNext(final int delta, final boolean skipWhitespace) {
		final Token t = in.peekNext(delta, skipWhitespace);
		if (lastGuard != -1 && t instanceof SymbolToken && ((SymbolToken) t).symbol == lastGuard)
			return null;
		return t;
	}
	
	protected final boolean peekNext(final char symbol) {
		final boolean success = peekNext(symbol, 0, false);
		if (!success)
			expectedPossible("'" + symbol + "'");
		return success;
	}
	
	protected final boolean peekNext(final char symbol, final int delta, final boolean skipWhitespace) {
		final Token t = in.peekNext(delta, skipWhitespace);
		return t instanceof SymbolToken && ((SymbolToken) t).symbol == symbol;
	}
	
	protected final boolean peekNext(final String word) {
		final boolean success = peekNext(word, 0, false);
		if (!success)
			expectedPossible("'" + word + "'");
		return success;
	}
	
	protected final boolean peekNext(final String wordOrSymbol, final int delta, final boolean skipWhitespace) {
		if (Character.isLetter(wordOrSymbol.codePointAt(0))) {
			final Token t = in.peekNext(delta, skipWhitespace);
			return t instanceof WordToken && ((WordToken) t).word.equals(wordOrSymbol);
		} else {
			final int start = in.getTokenOffset();
			try {
				Token t = in.peekNext(delta, skipWhitespace);
				if (t == null)
					return false;
				in.setTokenOffset(t);
				for (int i = 0; i < wordOrSymbol.length(); i++) {
					t = in.forward();
					if (!(t instanceof SymbolToken && ((SymbolToken) t).symbol == wordOrSymbol.charAt(i)))
						return false;
				}
				return true;
			} finally {
				in.setTokenOffset(start);
			}
		}
	}
	
	// ---------------------------------------- control flow ----------------------------------------
	
	public static interface VoidProcessor {
		void process() throws ParseException;
	}
	
	public static interface ElementProcessor<T extends ASTElement> {
		T process() throws ParseException;
	}
	
	// TODO optimise if nothing matched? (how?)
	protected void until(final AbstractASTElement.VoidProcessor lambda, final char until, final boolean allowEmpty) {
		if (allowEmpty && try_(until)) {
//				if (allowEmpty)
//				else
//					expectedFatal("what?", start, length);
			return;
		}
		startGuard(until);
		try {
			lambda.process();
		} catch (final ParseException ex) {
			
		} finally {
			endGuard(until);
		}
	}
	
	protected void repeatUntil(final AbstractASTElement.VoidProcessor lambda, final char until, final boolean allowEmpty) {
		if (allowEmpty && try_(until)) {
			return;
		}
		startGuard(until);
		try {
			do {
				final int s = in.getTokenOffset();
				try {
					lambda.process();
				} catch (final ParseException ex) {}
				if (in.getTokenOffset() == s)
					break;
			} while (peekNext() != null);
		} finally {
			endGuard(until);
		}
	}
	
	protected void repeatUntilEnd(final AbstractASTElement.VoidProcessor lambda) {
		while (peekNext() != null) {
			final int s = in.getTokenOffset();
			try {
				lambda.process();
			} catch (final ParseException ex) {}
			if (in.getTokenOffset() == s)
				break;
		}
		if (!in.isAfterEnd()) {
			errorFatal("Unexpected data at end of document", in.getTextOffset(), in.getTextLength() - in.getTextOffset());
		}
	}
	
	protected void untilEnd(final AbstractASTElement.VoidProcessor lambda) {
		try {
			lambda.process();
		} catch (final ParseException ex) {}
		if (!in.isAfterEnd()) {
			errorFatal("Unexpected data at end of document", in.getTextOffset(), in.getTextLength() - in.getTextOffset());
		}
	}
	
	protected void oneGroup(final char start, final AbstractASTElement.VoidProcessor lambda, final char end) throws ParseException {
		one(start);
		until(lambda, end, true);
	}
	
	protected void oneRepeatingGroup(final char start, final AbstractASTElement.VoidProcessor lambda, final char end) throws ParseException {
		one(start);
		repeatUntil(lambda, end, true);
	}
	
	protected boolean tryGroup(final char start, final AbstractASTElement.VoidProcessor lambda, final char end) {
		if (try_(start)) {
			until(lambda, end, true);
			return true;
		}
		return false;
	}
	
	protected boolean tryRepeatingGroup(final char start, final AbstractASTElement.VoidProcessor lambda, final char end) {
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
	protected void unordered(final @NonNull VoidProcessor... lambdas) {
		final List<AbstractASTElement.VoidProcessor> notDone = new ArrayList<>(Arrays.asList(lambdas));
		outer: while (true) {
			final int start = in.getTokenOffset();
			for (int i = 0; i < notDone.size(); i++) {
				try {
					notDone.get(i).process();
				} catch (final ParseException e) {}
				if (in.getTokenOffset() > start) {
					notDone.remove(i);
					continue outer;
				}
			}
			return;
		}
	}
	
	// ---------------------------------------- one ----------------------------------------
	
	private final void oneFromTryNoException(final boolean success) {
		if (!success)
			fatalParseErrors.addAll(possibleParseErrors);
		possibleParseErrors.clear();
	}
	
	// call like 'oneFromTry(try_(whatever))'
	private final void oneFromTry(final boolean success) throws ParseException {
		oneFromTryNoException(success);
		if (!success)
			throw new ParseException();
	}
	
	private final <T> T oneFromTry(final @Nullable T t) throws ParseException {
		oneFromTry(t != null);// && (!(t instanceof Element) || (((Element) t).fatalParseErrors().isEmpty() || ((Element) t).isParseUnique())));
		assert t != null;
		return t;
	}
	
//		private final <T extends Token> @Nullable T oneFromTry(final @Nullable T token) {
//			oneFromTry(token != null);
//			return token;
//		}
	
	protected final SymbolToken one(final char symbol) throws ParseException {
		return oneFromTry(try2(symbol));
	}
	
	protected final @Nullable WordToken one(final String keywordOrSymbol) throws ParseException {
		if (Character.isLetter(keywordOrSymbol.charAt(0)))
			return oneFromTry(try2(keywordOrSymbol));
		oneFromTry(try_(keywordOrSymbol));
		return null;
	}
	
	protected final String oneOf(final String... keywordsAndSymbols) throws ParseException {
//			int start = in.getOffset();
		for (final String s : keywordsAndSymbols) {
			assert s != null;
			if (try_(s))
				return oneFromTry(s);
		}
//			expectedFatal("one of "+Arrays.toString(symbolsAndWords), start, 1);
		return oneFromTry((String) null);
	}
	
	@SuppressWarnings("unchecked")
	protected final <R extends ASTElement, T extends AbstractASTElement<R>> R one(final T t) throws ParseException {
//			final int start = in.getTokenOffset();
		@Nullable
		R r;
		try {
			r = t.parse(in);
//				if (r != t)
//					r.fatalParseErrors().addAll(t.fatalParseErrors()); // TODO add possible parse errors too?
			// just duplicates errors usually (TODO check)
		} catch (final ParseException e) {
			r = (R) t;
			assert !t.fatalParseErrors().isEmpty() : t;
		}
//			if (in.getTokenOffset() > start) {
		// an 'until' can match everything but not result in any tokens added to the element
//				assert r.regionLength() > 0 : r + " / " + t + " : " + t.regionLength() + " // " + r.fatalParseErrors() + " :: " + start + " / " + in;
		r.setParent(this);
//			}
		fatalParseErrors.addAll(r.fatalParseErrors());
//			assert !r.fatalParseErrors().isEmpty() || r != t || in.getTokenOffset() > start : t + " / " + in + " :: " + ((AbstractElement<?>) t).possibleParseErrors;
		return oneFromTry(r);
	}
	
	protected final <R extends ASTElement, T extends AbstractASTElement<R>> R one(final Class<T> type) throws ParseException {
		try {
			final Constructor<T> c = type.getDeclaredConstructor();
			c.setAccessible(true);
			return one(c.newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected final String oneVariableIdentifier() throws ParseException {
		return oneFromTry(tryVariableIdentifier());
	}
	
	protected final LowercaseWordToken oneVariableIdentifierToken() throws ParseException {
		return oneFromTry(tryVariableIdentifierToken());
	}
	
	protected final String oneTypeIdentifier() throws ParseException {
		return oneFromTry(tryTypeIdentifier());
	}
	
	protected final UppercaseWordToken oneTypeIdentifierToken() throws ParseException {
		return oneFromTry(tryTypeIdentifierToken());
	}
	
	protected final WordToken oneIdentifierToken() throws ParseException {
		return oneFromTry(tryIdentifierToken());
	}
	
	// ---------------------------------------- try ----------------------------------------
	
	private void trySuccessful() {
		oneFromTryNoException(true);
	}
	
	protected final @Nullable SymbolToken try2(final char symbol) {
		final Token t = in.current();
		if (t instanceof SymbolToken && ((SymbolToken) t).symbol == symbol) {
			next();
			trySuccessful();
			return (SymbolToken) t;
		}
		expectedPossible("'" + symbol + "'");
		return null;
	}
	
	protected final boolean try_(final char symbol) {
		return try2(symbol) != null;
	}
	
	protected final @Nullable WordToken try2(final String keywordOrSymbol) {
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
			return new SymbolsWordToken(tokens);
		}
		expectedPossible("'" + keywordOrSymbol + "'");
		return null;
	}
	
	public final boolean try_(final String keywordOrSymbol) {
		return try2(keywordOrSymbol) != null;
	}
	
	protected final @Nullable String tryVariableIdentifier() {
		final LowercaseWordToken t = tryVariableIdentifierToken();
		return t == null ? null : t.word;
	}
	
	protected final @Nullable LowercaseWordToken tryVariableIdentifierToken() {
		final Token t = peekNext();
		if (t instanceof LowercaseWordToken) {
			next();
			((WordToken) t).keyword = false;
			return (LowercaseWordToken) t;
		}
		expectedPossible("a non-type identifier");
		return null;
	}
	
	protected final @Nullable String tryTypeIdentifier() {
		final UppercaseWordToken t = tryTypeIdentifierToken();
		return t == null ? null : t.word;
	}
	
	protected final @Nullable UppercaseWordToken tryTypeIdentifierToken() {
		final Token t = peekNext();
		if (t instanceof UppercaseWordToken) {
			next();
			((WordToken) t).keyword = false;
			return (UppercaseWordToken) t;
		}
		expectedPossible("a type identifier");
		return null;
	}
	
	protected final @Nullable WordToken tryIdentifierToken() {
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
	
	protected final @Nullable WordToken try2(final String... symbolsAndWords) {
		for (final String s : symbolsAndWords) {
			assert s != null;
			final WordToken t = try2(s);
			if (t != null)
				return t;
		}
		return null;
	}
	
}
