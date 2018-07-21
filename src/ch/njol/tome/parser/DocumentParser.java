package ch.njol.tome.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.njol.tome.ast.ASTDocument;
import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.SimpleASTDocument;
import ch.njol.tome.util.TokenListStream;

public class DocumentParser<E extends ASTElement> implements ASTParser {
	
	public final TokenListStream in;
	private final E document;
	private final AttachedElementParser<E> parser;
	
	public DocumentParser(final TokenListStream in, final E document) {
		this.in = in;
		this.document = document;
		parser = new AttachedElementParser<>(this, document);
	}
	
	public AttachedElementParser<E> parser() {
		return parser;
	}
	
	public ASTDocument<E> done() {
		parser.done(null);
		compactFatalParseErrors();
		return new SimpleASTDocument<>(document, fatalParseErrors);
	}
	
	@Override
	public void addChildToAST(ASTElementPart child) {
		assert child == document;
	}
	
	private final List<ParseError> fatalParseErrors = new ArrayList<>();
	
	@Override
	public List<ParseError> fatalParseErrors() {
		return fatalParseErrors;
	}
	
	private void compactFatalParseErrors() {
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
	
}
