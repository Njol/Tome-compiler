package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRAnonymousObjectCreation extends AbstractIRExpression {
	
	private final IRBrokkrClassDefinition type;
	
	public IRAnonymousObjectCreation(final IRBrokkrClassDefinition type) {
		this.type = registerDependency(type);
	}
	
	@Override
	public IRTypeUse type() {
		return type.getUse();
	}
	
	@Override
	public IRContext getIRContext() {
		return type.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return new InterpretedNormalObject(type); // no need to call a Brokkr constructor (as there is none). Fields' initial values are set by this Java constructor.
	}
	
}
