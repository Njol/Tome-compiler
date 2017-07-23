package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRParameterRedefinition extends IRVariableRedefinition {
	
	@Override
	String name();
	
	@Override
	public IRTypeUse type();
	
	@Override
	IRParameterDefinition definition();
	
	@Nullable
	InterpretedObject defaultValue(InterpreterContext context);
	
}
