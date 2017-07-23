package ch.njol.brokkr.interpreter.nativetypes;

// GENERATED FILE - DO NOT MODIFY

public class InterpretedNativeInt16 extends AbstractInterpretedSimpleNativeObject {
	
	public short value;
	
	public InterpretedNativeInt16(final short value) {
		this.value = value;
	}
	
	// native methods
	
	public InterpretedNativeInt16 _add16(final InterpretedNativeInt16 other) {
		return new InterpretedNativeInt16((short) (value + other.value));
	}
	
}
