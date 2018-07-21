package ch.njol.tome.interpreter.nativetypes;

import ch.njol.tome.interpreter.InterpretedTuple;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.internal.IRNativeTypeClassDefinition;
import ch.njol.tome.ir.uses.IRClassUse;
import ch.njol.tome.ir.uses.IRSimpleClassUse;

public abstract class InterpretedNativeClosure implements InterpretedNativeObject {
	
	private final IRTypeTuple parameters;
	private final IRTypeTuple results;
	private final boolean isModifying;
	
	public InterpretedNativeClosure(final IRTypeTuple parameters, final IRTypeTuple results, final boolean isModifying) {
		IRElement.assertSameIRContext(parameters, results);
		this.parameters = parameters;
		this.results = results;
		this.isModifying = isModifying;
	}
	
	protected abstract InterpretedTuple interpret(InterpretedTuple arguments) throws InterpreterException;
	
	public InterpretedTuple _invoke(final InterpretedTuple arguments) throws InterpreterException {
		return interpret(arguments);
	}
	
	@Override
	public IRClassUse nativeClass() {
		// TODO make subclass of Function<...> or Procedure<...>
		return new IRSimpleClassUse(IRNativeTypeClassDefinition.get(parameters.getIRContext(), getClass()));
	}
	
}
