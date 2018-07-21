package ch.njol.tome.ir.statements;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRPostcondition extends AbstractIRPreOrPostCondition {
	
	public IRPostcondition(final IRAttributeRedefinition attribute, final @Nullable String name, final IRExpression value) {
		super(attribute, name, value);
	}
	
}
