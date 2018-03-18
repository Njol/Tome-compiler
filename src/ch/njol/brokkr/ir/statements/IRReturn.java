package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.ir.IRContext;

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
