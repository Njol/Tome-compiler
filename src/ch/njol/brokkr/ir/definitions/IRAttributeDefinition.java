package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The original definition of an attribute, which can either be in an interface (for public methods) or a class (for private emthds and fields)
 */
public interface IRAttributeDefinition extends IRAttributeRedefinition, IRMemberDefinition {
	
	/**
	 * @return null - this is already the top-most definition.
	 */
	@Override
	default @Nullable IRAttributeRedefinition parentRedefinition() {
		return null;
	}
	
	@Override
	default IRAttributeDefinition definition() {
		return this;
	}
	
}
