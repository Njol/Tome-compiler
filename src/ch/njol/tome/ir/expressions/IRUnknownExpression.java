package ch.njol.tome.ir.expressions;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.AbstractIRUnknown;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;

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
