package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.expressions.ASTQuantifier.ASTQuantifierVar;

public class IRQuantifierVariable extends AbstractIRBrokkrVariable implements IRVariableDefinition {
	
	public IRQuantifierVariable(final ASTQuantifierVar ast) {
		super(ast);
	}
	
}
