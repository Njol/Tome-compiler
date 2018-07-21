package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;
	
	public InterpretedNativeUInt32(final IRContext irContext, final int value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt32 _add32(final InterpretedNativeUInt32 other) {
		return new InterpretedNativeUInt32(irContext, value + other.value);
	}
	
}
