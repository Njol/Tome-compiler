package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;

public class IRVariableDeclaration extends AbstractIRStatement {
	
	private final IRVariableRedefinition variable;
	
	public IRVariableDeclaration(final IRVariableRedefinition variable) {
		this.variable = variable;
	}
	
	@Override
	public IRContext getIRContext() {
		return variable.getIRContext();
	}
	
	@Override
	public void interpret(final InterpreterContext context) {
		context.defineLocalVariable(variable.definition());
	}
	
}
