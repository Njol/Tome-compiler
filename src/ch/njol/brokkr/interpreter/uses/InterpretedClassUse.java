package ch.njol.brokkr.interpreter.uses;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

/**
 * The use of a class, with all generic parameters set (if any).
 */
public interface InterpretedClassUse extends InterpretedTypeUse {
	
	public InterpretedNativeClassDefinition getBase();
	
}
