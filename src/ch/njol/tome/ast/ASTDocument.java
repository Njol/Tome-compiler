package ch.njol.tome.ast;

import java.util.List;

import ch.njol.tome.parser.ParseError;

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
	
	List<ParseError> fatalParseErrors();
	
//	/**
//	 * @return the token list used to build this AST
//	 */
//	public TokenList tokens();
	
}
