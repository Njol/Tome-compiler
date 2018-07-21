package ch.njol.tome.ir.statements;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.AbstractIRUnknown;

public class IRUnknownStatement extends AbstractIRUnknown implements IRStatement {
	
	public IRUnknownStatement(final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException(errorMessage);
	}
	
}
