package ch.njol.tome.ir;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.statements.IRStatement;

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
