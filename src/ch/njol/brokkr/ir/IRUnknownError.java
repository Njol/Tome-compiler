package ch.njol.brokkr.ir;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.statements.IRStatement;

public class IRUnknownError extends AbstractIRUnknown implements IRError, IRStatement {
	
	private final String name;
	
	public IRUnknownError(final String name, final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
		this.name = name;
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException(errorMessage);
	}
	
	@Override
	public String name() {
		return name;
	}
	
}
