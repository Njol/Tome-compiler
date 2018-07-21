package ch.njol.tome.ir.statements;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;

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
