package ch.njol.tome.ir.expressions;

import ch.njol.tome.ast.expressions.ASTNull;
import ch.njol.tome.interpreter.InterpretedNullConstant;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;

/**
 * The special <code>null</code> "value".
 */
public class IRNull extends AbstractIRExpression {
	
	private final IRContext irContext;
	
	public IRNull(final ASTNull ast) {
		irContext = ast.getIRContext();
		registerDependency(ast);
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public IRTypeUse type() {
		return InterpretedNullConstant.get(getIRContext()).nativeClass();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		return InterpretedNullConstant.get(getIRContext());
	}
	
}
