package ch.njol.tome.interpreter.nativetypes;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;

// TODO use 'reference' or 'pointer'?
public class InterpretedNativePointerArray<T extends InterpretedNativeObject> extends AbstractInterpretedSimpleNativeObject {
	
	private final Class<T> type;
	public T[] values;
	
	@SuppressWarnings("unchecked")
	public InterpretedNativePointerArray(final IRContext irContext, final Class<T> type, final int size) {
		super(irContext);
		this.type = type;
		values = (T[]) Array.newInstance(type, size);
	}
	
	@SuppressWarnings("unchecked")
	public InterpretedNativePointerArray(final IRContext irContext, final T[] values) {
		super(irContext);
		this.values = values;
		this.type = (@NonNull Class<T>) values.getClass().getComponentType();
	}
	
	// native methods
	
	// TODO type argument - Class or some IRNativeClass type?
	public static <T extends InterpretedNativeObject> InterpretedNativePointerArray<T> _new(final IRContext irContext, final Class<T> type, final InterpretedNativeUInt64 size) throws InterpreterException {
		if (size.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
		return new InterpretedNativePointerArray<>(irContext, type, (int) size.value);
	}
	
	public T _get(final InterpretedNativeUInt64 index) throws InterpreterException {
		if (index.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
		return values[(int) index.value];
	}
	
	public void _set(final InterpretedNativeUInt64 index, final T value) throws InterpreterException {
		if (index.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
//		if (!type.isSuperTypeOfOrEqual(value.nativeType()))
//			throw new InterpreterException("Tried to store a " + value.nativeType() + " in an array of type " + type);
		values[(int) index.value] = value;
	}
	
	public InterpretedNativeUInt64 _size() {
		return new InterpretedNativeUInt64(irContext, values.length);
	}
	
}
