package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.nativetypes.internal.InterpretedNativeSimpleNativeClass;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;

public abstract class AbstractInterpretedSimpleNativeObject implements InterpretedNativeObject {
	
	@Override
	public InterpretedClassUse nativeClass() {
		return new InterpretedSimpleClassUse(InterpretedNativeSimpleNativeClass.get(getClass()));
	}
	
}
