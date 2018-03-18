package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRAnonymousObjectCreation extends AbstractIRExpression {
	
	private final IRClassDefinition type;
	
	public IRAnonymousObjectCreation(final IRClassDefinition type) {
		this.type = type;
	}
	
	@Override
	public IRTypeUse type() {
		return type.getRawUse();
	}
	
	@Override
	public IRContext getIRContext() {
		return type.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return new InterpretedNormalObject(new IRSimpleClassUse(type)); // no need to call a constructor (as there is none). Fields' initial values are set by this constructor.
	}
	
}
