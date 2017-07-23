package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt64 extends AbstractInterpretedSimpleNativeObject {
	
	public long value;
	
	public InterpretedNativeUInt64(final long value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt64 _add64(final InterpretedNativeUInt64 other) {
		return new InterpretedNativeUInt64(value + other.value);
	}
	
}
