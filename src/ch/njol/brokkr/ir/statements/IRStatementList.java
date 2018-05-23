package ch.njol.brokkr.ir.statements;

import java.util.List;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;

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
