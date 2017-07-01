package ch.njol.brokkr.interpreter;

import java.util.List;

import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;

/**
 * The definition of an error // TODO rename?
 */
public class InterpretedError {
	
	private final String name;
	private final List<InterpretedParameterRedefinition> parameters;
	
	public InterpretedError(final String name, final List<InterpretedParameterRedefinition> parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	
	public String name() {
		return name;
	}
	
	public List<InterpretedParameterRedefinition> parameters() {
		return parameters;
	}
	
}
