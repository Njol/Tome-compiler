package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.nativetypes.internal.IRNativeTypeClassDefinition;
import ch.njol.tome.ir.uses.IRClassUse;
import ch.njol.tome.ir.uses.IRSimpleClassUse;

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
