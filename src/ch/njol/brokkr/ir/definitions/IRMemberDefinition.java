package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface IRMemberDefinition extends IRMemberRedefinition {
	
	@Override
	default IRMemberDefinition definition() {
		return this;
	}
	
	@Override
	default @Nullable IRMemberRedefinition parentRedefinition() {
		return null;
	}
	
	@Override
	default boolean isRedefinitionOf(final IRMemberRedefinition other) {
		return equalsMember(other);
	}
	
}
