package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;

	public InterpretedNativeUInt32(int value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeUInt32 _add32(InterpretedNativeUInt32 other) {
		return new InterpretedNativeUInt32((int)(value + other.value));
	}
	
}
