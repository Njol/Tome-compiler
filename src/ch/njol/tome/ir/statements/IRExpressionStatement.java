package ch.njol.tome.ir.statements;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.expressions.IRExpression;

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
