package ch.njol.brokkr.interpreter;

import java.util.List;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;

/**
 * The definition of an error // TODO rename?
 */
public class InterpretedError {
	
	private final String name;
	private final List<InterpretedParameterRedefinition> parameters;
	private final InterpretedAttributeRedefinition attribute;
	
	public InterpretedError(final String name, final List<InterpretedParameterRedefinition> parameters, final InterpretedAttributeRedefinition attribute) {
		this.name = name;
		this.parameters = parameters;
		this.attribute = attribute;
	}
	
	public String name() {
		return name;
	}
	
	public List<InterpretedParameterRedefinition> parameters() {
		return parameters;
	}
	
	public InterpretedAttributeRedefinition attribute() {
		return attribute;
	}
	
}
