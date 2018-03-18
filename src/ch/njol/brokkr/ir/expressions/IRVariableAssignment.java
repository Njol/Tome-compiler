package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRVariableDefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRVariableAssignment extends AbstractIRExpression {
	
	private final IRVariableDefinition variable;
	private final IRExpression value;
	
	public IRVariableAssignment(final IRVariableDefinition variable, final IRExpression value) {
		IRElement.assertSameIRContext(variable, value);
		this.variable = variable;
		this.value = value;
	}
	
	@Override
	public IRTypeUse type() {
		return value.type();
	}
	
	@Override
	public IRContext getIRContext() {
		return variable.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		final InterpretedObject val = value.interpret(context);
		context.setLocalVariableValue(variable, val);
		return val;
	}
	
}
