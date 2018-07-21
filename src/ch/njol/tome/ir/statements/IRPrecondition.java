package ch.njol.tome.ir.statements;

import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRPrecondition extends AbstractIRPreOrPostCondition implements IRError {
	
	public IRPrecondition(final IRAttributeRedefinition attribute, final String name, final IRExpression value) {
		super(attribute, name, value);
	}
	
	@Override
	public String name() {
		return "" + name;
	}
	
}
