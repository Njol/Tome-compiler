package ch.njol.tome.ir;

import ch.njol.tome.ir.expressions.IRExpression;

public class IRPredicateGenericArgument extends AbstractIRElement implements IRGenericArgument {
	
	public final IRExpression predicate;
	
	public IRPredicateGenericArgument(final IRExpression predicate) {
		this.predicate = predicate;
	}
	
	@Override
	public IRContext getIRContext() {
		return predicate.getIRContext();
	}
	
}
