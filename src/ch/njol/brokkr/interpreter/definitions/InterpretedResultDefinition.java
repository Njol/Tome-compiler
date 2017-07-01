package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedResultDefinition extends InterpretedResultRedefinition, InterpretedVariableDefinition {
	
	@Override
	default @NonNull InterpretedResultDefinition definition() {
		return this;
	}
	
}
