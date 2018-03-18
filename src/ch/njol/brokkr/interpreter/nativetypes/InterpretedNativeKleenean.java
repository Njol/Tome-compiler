package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.common.Kleenean;
import ch.njol.brokkr.ir.IRContext;

public class InterpretedNativeKleenean extends AbstractInterpretedSimpleNativeObject {
	
	public final Kleenean value;
	
	public InterpretedNativeKleenean(final IRContext irContext, final Kleenean value) {
		super(irContext);
		this.value = value;
	}
	
	// native methods
	
	public static InterpretedNativeKleenean _true(final IRContext irContext) {
		return new InterpretedNativeKleenean(irContext, Kleenean.TRUE);
	}
	
	public static InterpretedNativeKleenean _false(final IRContext irContext) {
		return new InterpretedNativeKleenean(irContext, Kleenean.FALSE);
	}
	
	public static InterpretedNativeKleenean _unknown(final IRContext irContext) {
		return new InterpretedNativeKleenean(irContext, Kleenean.UNKNOWN);
	}
	
	public InterpretedNativeKleenean _negated() {
		return new InterpretedNativeKleenean(irContext, value.negated());
	}
	
	public InterpretedNativeKleenean _and(final InterpretedNativeKleenean other) {
		return new InterpretedNativeKleenean(irContext, value.and(other.value));
	}
	
	public InterpretedNativeKleenean _or(final InterpretedNativeKleenean other) {
		return new InterpretedNativeKleenean(irContext, value.or(other.value));
	}
	
	public InterpretedNativeKleenean _implies(final InterpretedNativeKleenean other) {
		return new InterpretedNativeKleenean(irContext, value.implies(other.value));
	}
	
	public InterpretedNativeBoolean _equals(final InterpretedNativeKleenean other) {
		return new InterpretedNativeBoolean(irContext, value == other.value);
	}
	
}
