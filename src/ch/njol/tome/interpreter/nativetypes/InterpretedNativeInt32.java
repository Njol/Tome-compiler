package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;
	
	public InterpretedNativeInt32(final IRContext irContext, final int value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt32 _add32(final InterpretedNativeInt32 other) {
		return new InterpretedNativeInt32(irContext, value + other.value);
	}
	
}
