package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.AbstractIRUnknown;

public class IRUnknownStatement extends AbstractIRUnknown implements IRStatement {
	
	public IRUnknownStatement(final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException(errorMessage);
	}
	
}
