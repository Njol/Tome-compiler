package ch.njol.brokkr.ast;

import ch.njol.brokkr.compiler.TokenList;

/**
 * A complete abstract syntax tree (AST).
 * <p>
 * Technically, every {@link ASTElement} is an AST, but this interface adds metadata to the whole tree, like the source list of tokens used to build it.
 */
public interface ASTDocument<Root extends ASTElement> {

	/**
	 * @return The root of this AST
	 */
	public Root root();
	
	/**
	 * @return the token list used to build this AST
	 */
	public TokenList tokens();
	
}
