package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;
	
	public InterpretedNativeInt64(final long value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt64 _add64(final InterpretedNativeInt64 other) {
		return new InterpretedNativeInt64(value + other.value);
	}
	
}
