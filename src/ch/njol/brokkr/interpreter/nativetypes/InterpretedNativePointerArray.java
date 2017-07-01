package ch.njol.brokkr.interpreter.nativetypes;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.internal.InterpretedNativeSimpleNativeClass;

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
	
	public T _get(final InterpretedNativeInt64 index) {
		return values[(int) index.value];
	}
	
	public void _set(final InterpretedNativeInt64 index, final T value) {
		if (index.value > Integer.MAX_VALUE)
			throw new InterpreterException("not implemented");
//		if (!type.isSuperTypeOfOrEqual(value.nativeType()))
//			throw new InterpreterException("Tried to store a " + value.nativeType() + " in an array of type " + type);
		values[(int) index.value] = value;
	}
	
	public InterpretedObject _size() {
		return new InterpretedNativeUInt64(values.length);
	}
	
}
