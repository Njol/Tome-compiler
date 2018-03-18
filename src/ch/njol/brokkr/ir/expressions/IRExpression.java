package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRExpression extends IRElement {
	
	IRTypeUse type();
	
	public InterpretedObject interpret(InterpreterContext context) throws InterpreterException;
	
}
