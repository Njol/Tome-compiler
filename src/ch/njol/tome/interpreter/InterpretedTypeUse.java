package ch.njol.tome.interpreter;

import ch.njol.tome.ir.uses.IRTypeUse;

/**
 * A {@link IRTypeUse} at runtime, where it is also an object.
 */
public interface InterpretedTypeUse extends InterpretedObject {
	
	IRTypeUse irType();
	
}
