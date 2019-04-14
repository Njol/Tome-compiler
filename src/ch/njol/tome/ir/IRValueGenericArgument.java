package ch.njol.tome.ir;

import ch.njol.tome.ir.expressions.IRExpression;

public class IRValueGenericArgument extends AbstractIRElement implements IRGenericArgument {
	
	public final IRExpression value;
	
	public IRValueGenericArgument(final IRExpression value) {
		this.value = value;
	}
	
	public IRExpression getValue() {
		return value;
	}
	
	@Override
	public IRContext getIRContext() {
		return value.getIRContext();
	}
	
}
