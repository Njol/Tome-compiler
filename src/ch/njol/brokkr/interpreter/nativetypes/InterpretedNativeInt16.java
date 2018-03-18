package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;
	
	public InterpretedNativeInt16(final IRContext irContext, final short value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt16 _add16(final InterpretedNativeInt16 other) {
		return new InterpretedNativeInt16(irContext, (short) (value + other.value));
	}
	
}
