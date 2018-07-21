package ch.njol.tome.ir.statements;

import java.util.List;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;

public class IRStatementList extends AbstractIRStatement {
	
	private final IRContext irContext;
	private final List<IRStatement> statements;
	
	public IRStatementList(final IRContext irContext, final List<IRStatement> statements) {
		this.irContext = irContext;
		IRElement.assertSameIRContext(statements);
		this.statements = registerDependencies(statements);
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		for (final IRStatement statement : statements) {
			statement.interpret(context);
			if (context.isReturning)
				return;
		}
	}
	
}
