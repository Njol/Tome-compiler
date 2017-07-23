package ch.njol.brokkr.interpreter;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.nativetypes.IRTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.nativetypes.internal.IRSimpleNativeClass;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

public abstract class InterpretedNativeClosure implements InterpretedNativeObject {
	
	private final IRTypeTuple parameters;
	private final IRTypeTuple results;
	private final boolean isModifying;
	
	public InterpretedNativeClosure(final IRTypeTuple parameters, final IRTypeTuple results, final boolean isModifying) {
		this.parameters = parameters;
		this.results = results;
		this.isModifying = isModifying;
	}
	
	protected abstract IRTuple interpret(IRTuple arguments);
	
	public IRTuple _invoke(final IRTuple arguments) {
		return interpret(arguments);
	}
	
	@Override
	public IRClassUse nativeClass() {
		// TODO make subclass of Function<...> or Procedure<...>
		return new IRSimpleClassUse(IRSimpleNativeClass.get(getClass()));
	}
	
}
