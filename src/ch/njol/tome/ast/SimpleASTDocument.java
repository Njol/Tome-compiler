package ch.njol.tome.ast;

import java.util.List;

import ch.njol.tome.parser.ParseError;

public class SimpleASTDocument<Root extends ASTElement> implements ASTDocument<Root> {
	
	private final Root root;
	private final List<ParseError> fatalParseErrors;
	
	public SimpleASTDocument(final Root root, final List<ParseError> fatalParseErrors) {
		this.root = root;
		this.fatalParseErrors = fatalParseErrors;
	}
	
	@Override
	public Root root() {
		return root;
	}
	
	@Override
	public List<ParseError> fatalParseErrors() {
		return fatalParseErrors;
	}
	
}
