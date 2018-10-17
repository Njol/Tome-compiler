package ch.njol.tome.compiler;

import ch.njol.tome.ast.toplevel.ASTSourceFile;

public interface BrokkrFileChecker {
	
	public void check(ASTSourceFile file);
	
}
