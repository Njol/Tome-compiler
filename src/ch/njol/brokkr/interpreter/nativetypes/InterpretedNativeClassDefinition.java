package ch.njol.brokkr.interpreter.nativetypes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

public interface InterpretedNativeClassDefinition extends InterpretedNativeTypeDefinition {
	
	@Nullable
	InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition);

	@Override
	default boolean isSupertypeOfOrEqual(InterpretedNativeTypeDefinition other) {
		return equalsType(other); // classes cannot be subtyped
	}
	
//	default @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
//		final InterpretedAttributeRedefinition redefinition = getAttributeRedefinition(definition);
//		if (redefinition == null)
//			return null;
//		if (!(redefinition instanceof InterpretedAttributeImplementation))
//			throw new InterpreterException("Attribute " + redefinition.name() + " in class " + this + " is not implemented");
//		return (InterpretedAttributeImplementation) redefinition;
//	}

}
