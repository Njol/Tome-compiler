package ch.njol.brokkr.interpreter;

import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

/**
 * A {@link IRTypeUse} at runtime, where it is also an object.
 */
public interface InterpretedTypeUse extends InterpretedObject {
	
	IRTypeUse irType();
	
	InterpretedTypeUse getGenericType(IRGenericTypeDefinition definition) throws InterpreterException;
	
}
