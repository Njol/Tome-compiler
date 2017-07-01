package ch.njol.brokkr.interpreter.definitions;

import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedParameterRedefinition extends InterpretedVariableRedefinition {

	@Override
	String name();
	
	@Override
	public InterpretedTypeUse type();
	
	@Override
	InterpretedParameterDefinition definition();
	
}
