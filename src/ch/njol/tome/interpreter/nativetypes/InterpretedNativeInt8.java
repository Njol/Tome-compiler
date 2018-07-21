package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt8 extends AbstractInterpretedSimpleNativeObject {
	
	public byte value;
	
	public InterpretedNativeInt8(final IRContext irContext, final byte value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt8 _add8(final InterpretedNativeInt8 other) {
		return new InterpretedNativeInt8(irContext, (byte) (value + other.value));
	}
	
}
