package ch.njol.brokkr.interpreter.definitions;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.data.MethodModifiability;

/**
 * The original definition of an attribute, which can either be in an interface (for public methods) or a class (for private emthds and fields)
 */
public interface InterpretedAttributeDefinition extends InterpretedAttributeRedefinition, InterpretedMemberDefinition {
	
	/**
	 * @return null - this is already the top-most definition.
	 */
	@Override
	default @Nullable InterpretedAttributeRedefinition parentRedefinition() {
		return null;
	}
	
	@Override
	default InterpretedAttributeDefinition definition() {
		return this;
	}
	
	boolean equalsAttribute(InterpretedAttributeDefinition other);
	
}
