package ch.njol.brokkr.ir.statements;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;

public class IRPostcondition extends AbstractIRPreOrPostCondition {
	
	public IRPostcondition(final IRAttributeRedefinition attribute, final @Nullable String name, final IRExpression value) {
		super(attribute, name, value);
	}
	
}
