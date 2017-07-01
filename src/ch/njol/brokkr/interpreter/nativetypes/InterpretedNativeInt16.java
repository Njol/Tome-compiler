package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;

	public InterpretedNativeInt16(short value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeInt16 _add16(InterpretedNativeInt16 other) {
		return new InterpretedNativeInt16((short)(value + other.value));
	}
	
}
