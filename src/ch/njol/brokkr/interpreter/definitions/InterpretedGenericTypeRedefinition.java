package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface InterpretedGenericTypeRedefinition extends InterpretedMemberRedefinition {
	
	@Override
	InterpretedGenericTypeDefinition definition();
	
}
