package ch.njol.brokkr.compiler;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;

public interface BrokkrFileChecker {
	
	public void check(ASTBrokkrFile file);
	
}
