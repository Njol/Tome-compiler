package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;
	
	public InterpretedNativeUInt16(final IRContext irContext, final short value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt16 _add16(final InterpretedNativeUInt16 other) {
		return new InterpretedNativeUInt16(irContext, (short) (value + other.value));
	}
	
}
