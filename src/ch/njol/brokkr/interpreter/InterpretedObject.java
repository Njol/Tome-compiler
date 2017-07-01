package ch.njol.brokkr.interpreter;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;
import ch.njol.brokkr.interpreter.uses.InterpretedClassObject;
import ch.njol.brokkr.interpreter.uses.InterpretedClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedObject {
	
	/**
	 * @return The class this object is an instance of.
	 */
	InterpretedClassObject nativeClass();
	
	/*
	default @Nullable InterpretedAttributeImplementation getAttributeImplementation(InterpretedAttributeDefinition definition) {
		// TODO get 'var' attributes directly from the object? or are their implementations just special?
		return nativeType().getAttributeImplementation(definition);
	}
	
	Map<InterpretedAttributeDefinition, InterpretedAttributeImplementation> attributeValues();
	
	default void setAttributeValue(InterpretedAttributeImplementation attribute, InterpretedAttributeImplementation value) {
		attributeValues().put(attribute, value);
	}
	
	@SuppressWarnings({"null", "unused"})
	default InterpretedObject getAttributeValue(InterpretedAttributeImplementation attribute) {
		InterpretedObject instanceValue = attributeValues().get(attribute);
		if (instanceValue != null)
			return instanceValue;
		return type().getAttributeValue(attribute);
	}
	
	// TODO arguments passed to meta access
	// TODO can closures be called with a single result and multiple results?
	default InterpretedClosure getAttributeMeta(final InterpretedAttributeImplementation attribute, boolean allResults) {
		return new InterpretedClosure(attribute.parameters(), attribute.results(), attribute.isModifying()) {
			@Override
			InterpretedObject interpret(Map<InterpretedParameter, InterpretedObject> arguments) {
				return attribute.interpret(InterpretedObject.this, arguments, allResults);
			}
		};
	}
	*/
}
