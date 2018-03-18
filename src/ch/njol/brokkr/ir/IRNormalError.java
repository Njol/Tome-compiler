package ch.njol.brokkr.ir;

import java.util.Arrays;
import java.util.List;

import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;

/**
 * The definition of an error // TODO rename?
 */
public class IRNormalError extends AbstractIRElement implements IRError {
	
	private final String name;
	private final List<IRParameterRedefinition> parameters;
	private final IRAttributeRedefinition attribute;
	
	public IRNormalError(final String name, final List<IRParameterRedefinition> parameters, final IRAttributeRedefinition attribute) {
		IRElement.assertSameIRContext(parameters, Arrays.asList(attribute));
		this.name = name;
		this.parameters = parameters;
		this.attribute = attribute;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	public List<IRParameterRedefinition> parameters() {
		return parameters;
	}
	
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
}
