package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;

	public InterpretedNativeUInt16(short value) {
		this.value = value;
	}

	// native methods
	
	public InterpretedNativeUInt16 _add16(InterpretedNativeUInt16 other) {
		return new InterpretedNativeUInt16((short)(value + other.value));
	}
	
}
