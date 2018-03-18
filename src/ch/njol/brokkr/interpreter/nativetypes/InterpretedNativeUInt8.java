package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt8 extends AbstractInterpretedSimpleNativeObject {
	
	public byte value;
	
	public InterpretedNativeUInt8(final IRContext irContext, final byte value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt8 _add8(final InterpretedNativeUInt8 other) {
		return new InterpretedNativeUInt8(irContext, (byte) (value + other.value));
	}
	
}
