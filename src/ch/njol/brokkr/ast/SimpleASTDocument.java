package ch.njol.brokkr.ast;

import ch.njol.brokkr.compiler.TokenList;

public class SimpleASTDocument<Root extends ASTElement> implements ASTDocument<Root> {
	
	private final Root root;
	private final TokenList tokens;
	
	public SimpleASTDocument(final Root root, final TokenList tokens) {
		this.root = root;
		this.tokens = tokens;
	}
	
	@Override
	public Root root() {
		return root;
	}
	
	@Override
	public TokenList tokens() {
		return tokens;
	}
	
}
