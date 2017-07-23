package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt32 extends AbstractInterpretedSimpleNativeObject {
	
	public int value;
	
	public InterpretedNativeInt32(final int value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt32 _add32(final InterpretedNativeInt32 other) {
		return new InterpretedNativeInt32(value + other.value);
	}
	
}
