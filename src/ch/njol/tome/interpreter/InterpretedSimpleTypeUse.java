package ch.njol.tome.interpreter;

import ch.njol.tome.ir.uses.IRClassUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRTypeUseClassUse;

// TODO define what this class actually is used for
public class InterpretedSimpleTypeUse implements InterpretedTypeUse {
	
	public final IRTypeUse typeUse;
	
	public InterpretedSimpleTypeUse(final IRTypeUse typeUse) {
		this.typeUse = typeUse;
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRTypeUseClassUse(typeUse);
	}
	
	@Override
	public IRTypeUse irType() {
		return typeUse;
	}
	
}
