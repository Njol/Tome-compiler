package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface IRClassDefinition extends IRTypeDefinition {
	
	@Nullable
	IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition);
	
	@Override
	default boolean isSupertypeOfOrEqual(final IRTypeDefinition other) {
		return equalsType(other); // classes cannot be subtyped
	}
	
//	default @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
//		final IRAttributeRedefinition redefinition = getAttributeRedefinition(definition);
//		if (redefinition == null)
//			return null;
//		if (!(redefinition instanceof IRAttributeImplementation))
//			throw new InterpreterException("Attribute " + redefinition.name() + " in class " + this + " is not implemented");
//		return (IRAttributeImplementation) redefinition;
//	}

}
