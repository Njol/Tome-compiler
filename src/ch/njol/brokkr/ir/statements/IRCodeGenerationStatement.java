package ch.njol.brokkr.ir.statements;

import java.util.List;

import ch.njol.brokkr.compiler.Token.CodeGenerationToken;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.expressions.IRExpression;

public class IRCodeGenerationStatement extends AbstractIRStatement {
	
	private final IRContext irContext;
	private final List<CodeGenerationToken> code;
	private final List<IRExpression> expressions;
	
	public IRCodeGenerationStatement(final IRContext irContext, final List<CodeGenerationToken> code, final List<IRExpression> expressions) {
		this.irContext = irContext;
		IRElement.assertSameIRContext(expressions);
		this.code = code;
		this.expressions = expressions;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		// TODO where to store the resulting string?
		final StringBuilder result = new StringBuilder(code.get(0).code);
		for (int i = 1; i < code.size(); i++) {
			final InterpretedObject val = expressions.get(i).interpret(context);
			result.append(val); // TODO how to make a String out of this?
			result.append(code.get(i).code);
		}
	}
	
}
