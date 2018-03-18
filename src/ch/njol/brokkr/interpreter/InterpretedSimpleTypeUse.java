package ch.njol.brokkr.interpreter;

import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.uses.IRClassUse;
import ch.njol.brokkr.ir.uses.IRGenericTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUseClassUse;

// TODO define what this class actually is used for
public class InterpretedSimpleTypeUse implements InterpretedTypeUse {
	
	public final IRTypeUse typeUse;
	
	public InterpretedSimpleTypeUse(final IRTypeUse typeUse) {
		this.typeUse = typeUse;
	}
	
	@Override
	public IRClassUse nativeClass() {
		return new IRTypeUseClassUse(typeUse);
	}
	
	@Override
	public IRTypeUse irType() {
		return typeUse;
	}
	
	@Override
	public InterpretedTypeUse getGenericType(final IRGenericTypeDefinition definition) throws InterpreterException {
		final IRGenericTypeUse gtu = typeUse.getGenericType(definition);
		if (gtu == null)
			throw new InterpreterException("Nonexistent generic type " + definition + " in type " + this);
		return gtu.interpret(new InterpreterContext(typeUse.getIRContext(), null));
	}
	
}
