package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;

	public InterpretedNativeUInt64(long value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeUInt64 _add64(InterpretedNativeUInt64 other) {
		return new InterpretedNativeUInt64((long)(value + other.value));
	}
	
}
