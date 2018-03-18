package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.nativetypes.internal.IRNativeTypeClassDefinition;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

public abstract class AbstractInterpretedSimpleNativeObject implements InterpretedNativeObject {
	
	protected final IRContext irContext;
	
	public AbstractInterpretedSimpleNativeObject(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRSimpleClassUse(IRNativeTypeClassDefinition.get(irContext, getClass()));
	}
	
}
