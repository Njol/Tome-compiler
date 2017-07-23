package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;
	
	public InterpretedNativeUInt32(final int value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt32 _add32(final InterpretedNativeUInt32 other) {
		return new InterpretedNativeUInt32(value + other.value);
	}
	
}
