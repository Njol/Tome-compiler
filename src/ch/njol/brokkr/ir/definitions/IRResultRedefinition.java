package ch.njol.brokkr.ir.definitions;

public interface IRResultRedefinition extends IRVariableRedefinition {
	
	@Override
	IRResultDefinition definition();
	
	IRAttributeRedefinition attribute();
	
	/**
	 * @param other
	 * @return Whether this and the given result are equal, i.e. are defined in the same attribute and have the same name.
	 */
	default boolean equalsResult(final IRResultRedefinition other) {
		return attribute().equalsMember(other.attribute()) && name().equals(other.name());
	}
	
}
