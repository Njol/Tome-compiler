package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface InterpretedGenericTypeDefinition extends InterpretedGenericTypeRedefinition, InterpretedMemberDefinition {
	
	@Override
	default InterpretedGenericTypeDefinition definition() {
		return this;
	}
	
	@Override
	default @Nullable InterpretedGenericTypeRedefinition parentRedefinition() {
		return null;
	}
	
}
