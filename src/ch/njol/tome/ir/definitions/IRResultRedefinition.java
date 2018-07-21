package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.expressions.IRExpression;

public interface IRResultRedefinition extends IRVariableRedefinition {
	
	@Override
	IRResultDefinition definition();
	
	IRAttributeRedefinition attribute();
	
	@Nullable
	IRExpression defaultValue();
	
	/**
	 * @param other
	 * @return Whether this and the given result are equal, i.e. are defined in the same attribute and have the same name.
	 */
	default boolean equalsResult(final IRResultRedefinition other) {
		return attribute().equalsMember(other.attribute()) && name().equals(other.name());
	}
	
}
