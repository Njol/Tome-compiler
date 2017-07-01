package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedVariableRedefinition extends InterpretedVariableOrAttributeRedefinition {

	String name();
	
	public InterpretedTypeUse type();
	
	@Override
	default @NonNull InterpretedTypeUse mainResultType() {
		return type();
	}
	
	InterpretedVariableDefinition definition();
	
}
