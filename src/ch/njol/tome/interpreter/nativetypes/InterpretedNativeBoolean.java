package ch.njol.tome.interpreter.nativetypes;

import java.util.Collections;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;

public class InterpretedNativeBoolean extends AbstractInterpretedSimpleNativeObject {
	
	public final boolean value;
	
	public InterpretedNativeBoolean(final IRContext irContext, final boolean value) {
		super(irContext);
		this.value = value;
	}
	
	// TODO implements Kleenean - make sure to implement the relevant methods
	// TODO how do these native classes implement interfaces anyway? do they just define methods that take InterpretedObjects as parameters?
	
	/**
	 * Transforms an interpreted Boolean into a Java boolean.
	 * 
	 * @param context
	 * @param object
	 * @return
	 * @throws InterpreterException
	 */
	public final static boolean getBoolean(final InterpreterContext context, final InterpretedObject object) throws InterpreterException {
		final InterpretedObject nativeBoolean = context.getAttributebyName("lang", "Boolean", "toNative").interpretDispatched(object, Collections.EMPTY_MAP, false);
		if (!(nativeBoolean instanceof InterpretedNativeBoolean))
			throw new InterpreterException("Boolean.toNative did not return a native boolean: " + nativeBoolean);
		return ((InterpretedNativeBoolean) nativeBoolean).value;
	}
	
	// native methods
	
	public static InterpretedNativeBoolean _true(final IRContext irContext) {
		return new InterpretedNativeBoolean(irContext, true);
	}
	
	public static InterpretedNativeBoolean _false(final IRContext irContext) {
		return new InterpretedNativeBoolean(irContext, false);
	}
	
	public InterpretedNativeBoolean _negated() {
		return new InterpretedNativeBoolean(irContext, !value);
	}
	
	public InterpretedNativeBoolean _and(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(irContext, value && other.value);
	}
	
	public InterpretedNativeBoolean _or(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(irContext, value || other.value);
	}
	
	public InterpretedNativeBoolean _implies(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(irContext, !value || other.value);
	}
	
	public InterpretedNativeBoolean _equals(final InterpretedNativeBoolean other) {
		return new InterpretedNativeBoolean(irContext, value == other.value);
	}
	
}
