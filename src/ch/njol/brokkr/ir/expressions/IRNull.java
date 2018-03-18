package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedNullConstant;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

/**
 * The special <code>null</code> "value".
 */
public class IRNull extends AbstractIRExpression {
	
	private final IRContext irContext;
	
	public IRNull(final IRContext irContext) {
		this.irContext = irContext;
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
