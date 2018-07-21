package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface IRParameterDefinition extends IRParameterRedefinition, IRVariableDefinition {
	
	@Override
	default @NonNull IRParameterDefinition definition() {
		return this;
	}
	
}
