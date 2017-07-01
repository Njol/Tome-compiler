package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt8 extends AbstractInterpretedSimpleNativeObject {
	
	public byte value;

	public InterpretedNativeInt8(byte value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeInt8 _add8(InterpretedNativeInt8 other) {
		return new InterpretedNativeInt8((byte)(value + other.value));
	}
	
}
