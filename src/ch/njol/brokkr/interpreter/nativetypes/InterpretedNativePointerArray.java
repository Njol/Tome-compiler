package ch.njol.brokkr.interpreter.nativetypes;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.InterpreterException;

// TODO use 'reference' or 'pointer'?
public class InterpretedNativePointerArray<T extends InterpretedNativeObject> extends AbstractInterpretedSimpleNativeObject {
	
	private final Class<T> type;
	public T[] values;
	
	@SuppressWarnings("unchecked")
	public InterpretedNativePointerArray(final Class<T> type, final int size) {
		this.type = type;
		values = (T[]) Array.newInstance(type, size);
	}
	
	@SuppressWarnings("unchecked")
	public InterpretedNativePointerArray(final T[] values) {
		this.values = values;
		this.type = (@NonNull Class<T>) values.getClass().getComponentType();
	}
	
	// native methods
	
	// TODO type argument - Class or some IRNativeClass type?
	public static <T extends InterpretedNativeObject> InterpretedNativePointerArray<T> _new(final Class<T> type, final InterpretedNativeUInt64 size) {
		if (size.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
		return new InterpretedNativePointerArray<>(type, (int) size.value);
	}
	
	public T _get(final InterpretedNativeUInt64 index) {
		if (index.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
		return values[(int) index.value];
	}
	
	public void _set(final InterpretedNativeUInt64 index, final T value) {
		if (index.value > Integer.MAX_VALUE)
			throw new InterpreterException("don't use the interpreter with huge arrays...");
//		if (!type.isSuperTypeOfOrEqual(value.nativeType()))
//			throw new InterpreterException("Tried to store a " + value.nativeType() + " in an array of type " + type);
		values[(int) index.value] = value;
	}
	
	public InterpretedNativeUInt64 _size() {
		return new InterpretedNativeUInt64(values.length);
	}
	
}
