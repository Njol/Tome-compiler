package ch.njol.brokkr.interpreter;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedTypeTuple;
import ch.njol.brokkr.interpreter.nativetypes.internal.InterpretedNativeSimpleNativeClass;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;

public abstract class InterpretedNativeClosure implements InterpretedNativeObject {
	
	private final InterpretedTypeTuple parameters;
	private final InterpretedTypeTuple results;
	private final boolean isModifying;
	
	public InterpretedNativeClosure(final InterpretedTypeTuple parameters, final InterpretedTypeTuple results, final boolean isModifying) {
		this.parameters = parameters;
		this.results = results;
		this.isModifying = isModifying;
	}
	
	protected abstract InterpretedTuple interpret(InterpretedTuple arguments);
	
	public InterpretedTuple _invoke(final InterpretedTuple arguments) {
		return interpret(arguments);
	}
	
	@Override
	public InterpretedClassUse nativeClass() {
		// TODO make subclass of Function<...> or Procedure<...>
		return new InterpretedSimpleClassUse(InterpretedNativeSimpleNativeClass.get(getClass()));
	}
	
}
