package ch.njol.brokkr.ir.statements;

import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;

public class IRPrecondition extends AbstractIRPreOrPostCondition implements IRError {
	
	public IRPrecondition(final IRAttributeRedefinition attribute, final String name, final IRExpression value) {
		super(attribute, name, value);
	}
	
	@Override
	public String name() {
		return "" + name;
	}
	
}
