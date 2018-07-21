package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRExpression extends IRElement {
	
	IRTypeUse type();
	
	public InterpretedObject interpret(InterpreterContext context) throws InterpreterException;
	
}
