package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedParameterRedefinition extends InterpretedVariableRedefinition {

	@Override
	String name();
	
	@Override
	public InterpretedTypeUse type();
	
	@Override
	InterpretedParameterDefinition definition();
	
	@Nullable
	InterpretedObject defaultValue(InterpreterContext context);
	
}
