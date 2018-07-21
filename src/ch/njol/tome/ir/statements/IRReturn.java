package ch.njol.tome.ir.statements;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.ir.IRContext;

public class IRReturn extends AbstractIRStatement {
	
	private final IRContext irContext;
	
	public IRReturn(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public void interpret(final InterpreterContext context) {
		context.isReturning = true;
	}
	
}
