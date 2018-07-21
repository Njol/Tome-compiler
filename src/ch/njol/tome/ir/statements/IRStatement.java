package ch.njol.tome.ir.statements;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRElement;

public interface IRStatement extends IRElement {
	
	void interpret(InterpreterContext context) throws InterpreterException;
	
}
