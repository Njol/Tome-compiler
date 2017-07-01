package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;

	public InterpretedNativeInt64(long value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeInt64 _add64(InterpretedNativeInt64 other) {
		return new InterpretedNativeInt64((long)(value + other.value));
	}
	
}
