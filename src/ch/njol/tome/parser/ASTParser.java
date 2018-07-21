package ch.njol.tome.parser;

import java.util.List;

import ch.njol.tome.ast.ASTElementPart;

public interface ASTParser {
	
	List<ParseError> fatalParseErrors();

	void addChildToAST(final ASTElementPart child);
	
}
