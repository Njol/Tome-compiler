package ch.njol.brokkr.interpreter.nativetypes;

public class InterpretedNativeBoolean extends AbstractInterpretedSimpleNativeObject {
	
	public final boolean value;
	
	public InterpretedNativeBoolean(final boolean value) {
		this.value = value;
	}
	
	// native methods
	
	public static InterpretedNativeBoolean _true() {
		return new InterpretedNativeBoolean(true);
	}
	
	public static InterpretedNativeBoolean _false() {
		return new InterpretedNativeBoolean(false);
	}
	
	public InterpretedNativeBoolean _negated() {
		return new InterpretedNativeBoolean(!value);
	}
	
	public InterpretedNativeBoolean _and(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(value && other.value);
	}
	
	public InterpretedNativeBoolean _or(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(value || other.value);
	}
	
	public InterpretedNativeBoolean _implies(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(!value || other.value);
	}
	
	public InterpretedNativeBoolean _equals(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(value == other.value);
	}
	
}
