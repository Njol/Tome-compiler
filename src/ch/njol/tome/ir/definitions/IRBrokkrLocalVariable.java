package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.ASTInterfaces.ASTVariable;

/**
 * A local variable defined in Brokkr code. Always a definition, as local variables cannot be overridden in any way.
 */
public class IRBrokkrLocalVariable extends AbstractIRBrokkrVariable implements IRVariableDefinition {
	
	public IRBrokkrLocalVariable(final ASTVariable var) {
		super(var);
	}
	
	@Override
	public IRVariableDefinition definition() {
		return this;
	}
	
}
