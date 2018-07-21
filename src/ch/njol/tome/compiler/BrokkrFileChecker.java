package ch.njol.tome.compiler;

import ch.njol.tome.ast.ASTTopLevelElements.ASTSourceFile;

public interface BrokkrFileChecker {
	
	public void check(ASTSourceFile file);
	
}
