package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedParameterDefinition extends InterpretedParameterRedefinition, InterpretedVariableDefinition {
	
	@Override
	default @NonNull InterpretedParameterDefinition definition() {
		return this;
	}
	
}
