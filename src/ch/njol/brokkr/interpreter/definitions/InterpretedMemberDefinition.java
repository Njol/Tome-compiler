package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedMemberDefinition extends InterpretedMemberRedefinition {
	
	@Override
	default InterpretedMemberDefinition definition() {
		return this;
	}
	
}
