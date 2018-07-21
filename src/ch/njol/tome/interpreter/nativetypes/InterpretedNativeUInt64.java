package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;
	
	public InterpretedNativeUInt64(final IRContext irContext, final long value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt64 _add64(final InterpretedNativeUInt64 other) {
		return new InterpretedNativeUInt64(irContext, value + other.value);
	}
	
}
