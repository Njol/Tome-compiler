package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRVariableExpression extends AbstractIRExpression {
	
	private final IRVariableRedefinition variable;
	
	public IRVariableExpression(final IRVariableRedefinition variable) {
		this.variable = registerDependency(variable);
	}
	
	@Override
	public IRTypeUse type() {
		return variable.type();
	}
	
	@Override
	public IRContext getIRContext() {
		return variable.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return context.getLocalVariableValue(variable.definition());
	}
	
}
