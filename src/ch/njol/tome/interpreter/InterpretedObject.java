package ch.njol.tome.interpreter;

import ch.njol.tome.ir.uses.IRClassUse;

public interface InterpretedObject {
	
	/**
	 * @return The class this object is an instance of.
	 */
	IRClassUse nativeClass();
	
	/*
	default @Nullable IRAttributeImplementation getAttributeImplementation(IRAttributeDefinition definition) {
		// TODO get 'var' attributes directly from the object? or are their implementations just special?
		return nativeType().getAttributeImplementation(definition);
	}
	
	Map<IRAttributeDefinition, IRAttributeImplementation> attributeValues();
	
	default void setAttributeValue(IRAttributeImplementation attribute, IRAttributeImplementation value) {
		attributeValues().put(attribute, value);
	}
	
	@SuppressWarnings({"null", "unused"})
	default IRObject getAttributeValue(IRAttributeImplementation attribute) {
		IRObject instanceValue = attributeValues().get(attribute);
		if (instanceValue != null)
			return instanceValue;
		return type().getAttributeValue(attribute);
	}
	
	// TODO arguments passed to meta access
	// TODO can closures be called with a single result and multiple results?
	default IRClosure getAttributeMeta(final IRAttributeImplementation attribute, boolean allResults) {
		return new IRClosure(attribute.parameters(), attribute.results(), attribute.isModifying()) {
			@Override
			IRObject interpret(Map<IRParameter, IRObject> arguments) {
				return attribute.interpret(IRObject.this, arguments, allResults);
			}
		};
	}
	*/
}
