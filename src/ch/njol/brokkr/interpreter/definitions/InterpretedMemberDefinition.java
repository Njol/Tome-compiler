package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface InterpretedMemberDefinition extends InterpretedMemberRedefinition {
	
	@Override
	default InterpretedMemberDefinition definition() {
		return this;
	}
	
	@Override
	default @Nullable InterpretedMemberRedefinition parentRedefinition() {
		return null;
	}
	
	@Override
	default boolean isRedefinitionOf(final InterpretedMemberRedefinition other) {
		return equalsMember(other);
	}
	
}
