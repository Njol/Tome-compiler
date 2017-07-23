package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

public interface IRGenericTypeDefinition extends IRGenericTypeRedefinition, IRMemberDefinition {
	
	@Override
	default IRGenericTypeDefinition definition() {
		return this;
	}
	
	@Override
	default @Nullable IRGenericTypeRedefinition parentRedefinition() {
		return null;
	}
	
}
