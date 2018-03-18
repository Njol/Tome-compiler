package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface IRMemberDefinition extends IRMemberRedefinition, Comparable<IRMemberDefinition> {
	
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
	
	/**
	 * Compares two members alphabetically (if they are declared in the same type, otherwise it returns the order of their declaring types)
	 */
	@Override
	default int compareTo(final IRMemberDefinition other) {
		final int c = declaringType().compareTo(other.declaringType());
		if (c != 0)
			return c;
		return name().compareTo(other.name());
	}
	
}
