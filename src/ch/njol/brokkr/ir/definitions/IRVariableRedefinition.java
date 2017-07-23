package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRVariableRedefinition extends IRVariableOrAttributeRedefinition {
	
	String name();
	
	public IRTypeUse type();
	
	@Override
	default IRTypeUse mainResultType() {
		return type();
	}
	
	IRVariableDefinition definition();
	
}
