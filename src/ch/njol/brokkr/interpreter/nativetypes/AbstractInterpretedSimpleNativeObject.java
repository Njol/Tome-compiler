package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.nativetypes.internal.IRSimpleNativeClass;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

public abstract class AbstractInterpretedSimpleNativeObject implements InterpretedNativeObject {
	
	@Override
	public IRClassUse nativeClass() {
		return new IRSimpleClassUse(IRSimpleNativeClass.get(getClass()));
	}
	
}
