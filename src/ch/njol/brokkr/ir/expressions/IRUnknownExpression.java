package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.AbstractIRUnknown;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public class IRUnknownExpression extends AbstractIRUnknown implements IRExpression {
	
	public IRUnknownExpression(final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
	}
	
	@Override
	public IRTypeUse type() {
		return new IRUnknownTypeUse(getIRContext());
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException(errorMessage);
	}
	
}
