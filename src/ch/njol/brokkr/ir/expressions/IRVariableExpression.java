package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRVariableExpression extends AbstractIRExpression {
	
	private final IRVariableRedefinition variable;
	
	public IRVariableExpression(final IRVariableRedefinition variable) {
		this.variable = variable;
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
