package ch.njol.tome.ir.statements;

import ch.njol.tome.ir.IRPrecondition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRPreconditionDeclaration extends AbstractIRPreOrPostCondition implements IRPrecondition {
	
	public IRPreconditionDeclaration(final IRAttributeRedefinition attribute, final String name, final IRExpression value) {
		super(attribute, name, value);
	}
	
	@Override
	public String name() {
		return "" + name;
	}
	
}
