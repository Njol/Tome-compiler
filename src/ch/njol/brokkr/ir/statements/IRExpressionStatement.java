package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.expressions.IRExpression;

public class IRExpressionStatement extends AbstractIRStatement {
	
	private final IRExpression expression;
	
	public IRExpressionStatement(final IRExpression expression) {
		this.expression = registerDependency(expression);
	}
	
	@Override
	public IRContext getIRContext() {
		return expression.getIRContext();
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		expression.interpret(context);
	}
	
}
