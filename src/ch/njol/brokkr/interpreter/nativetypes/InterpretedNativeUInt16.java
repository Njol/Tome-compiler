package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeUInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;
	
	public InterpretedNativeUInt16(final short value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeUInt16 _add16(final InterpretedNativeUInt16 other) {
		return new InterpretedNativeUInt16((short) (value + other.value));
	}
	
}
