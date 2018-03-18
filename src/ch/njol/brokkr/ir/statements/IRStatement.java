package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRElement;

public interface IRStatement extends IRElement {
	
	void interpret(InterpreterContext context) throws InterpreterException;
	
}
