package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;

	public InterpretedNativeInt32(int value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeInt32 _add32(InterpretedNativeInt32 other) {
		return new InterpretedNativeInt32((int)(value + other.value));
	}
	
}
