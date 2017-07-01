package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedVariableDefinition extends InterpretedVariableRedefinition {
	
	@Override
	default @NonNull InterpretedVariableDefinition definition() {
		return this;
	}
	
}
