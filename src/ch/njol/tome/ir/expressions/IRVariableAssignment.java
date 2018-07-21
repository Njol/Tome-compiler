package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.definitions.IRVariableDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRVariableAssignment extends AbstractIRExpression {
	
	private final IRVariableDefinition variable;
	private final IRExpression value;
	
	public IRVariableAssignment(final IRVariableDefinition variable, final IRExpression value) {
		IRElement.assertSameIRContext(variable, value);
		this.variable = registerDependency(variable);
		this.value = registerDependency(value);
	}
	
	@Override
	public IRTypeUse type() {
		return value.type(); // TODO or the type of the variable? the value's type may be more specific
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
