package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTExpressions.ASTQuantifierVar;
import ch.njol.brokkr.common.Invalidatable;
import ch.njol.brokkr.common.InvalidateListener;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRQuantifierVariable extends AbstractIRBrokkrVariable implements IRVariableDefinition {

	public IRQuantifierVariable(ASTQuantifierVar ast) {
		super(ast);
	}
	
}
