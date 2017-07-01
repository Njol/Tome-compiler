package ch.njol.brokkr.interpreter.nativetypes;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.nativetypes.internal.InterpretedNativeSimpleNativeClass;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public abstract class AbstractInterpretedSimpleNativeObject implements InterpretedNativeObject {

	@Override
	public InterpretedClassUse nativeClass() {
		return new InterpretedSimpleClassUse(InterpretedNativeSimpleNativeClass.get(getClass()));
	}
	
}
