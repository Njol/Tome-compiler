package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface IRResultDefinition extends IRResultRedefinition, IRVariableDefinition {
	
	@Override
	default @NonNull IRResultDefinition definition() {
		return this;
	}
	
}
