package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.ir.IRContext;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;
	
	public InterpretedNativeInt64(final IRContext irContext, final long value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt64 _add64(final InterpretedNativeInt64 other) {
		return new InterpretedNativeInt64(irContext, value + other.value);
	}
	
}
