package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedGenericTypeDefinition extends InterpretedGenericTypeRedefinition, InterpretedMemberDefinition {
	
	@Override
	default InterpretedGenericTypeDefinition definition() {
		return this;
	}
	
}
