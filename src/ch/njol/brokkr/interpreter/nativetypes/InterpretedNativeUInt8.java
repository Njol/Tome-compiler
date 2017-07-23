package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt8 extends AbstractInterpretedSimpleNativeObject {
	
	public byte value;
	
	public InterpretedNativeUInt8(final byte value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt8 _add8(final InterpretedNativeUInt8 other) {
		return new InterpretedNativeUInt8((byte) (value + other.value));
	}
	
}
