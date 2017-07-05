package ch.njol.brokkr.interpreter.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedMemberRedefinition extends SourceCodeLinkable {
	
	/**
	 * @return The name of this member, as of this (re)definition.
	 */
	String name();
	
	@Nullable InterpretedMemberRedefinition parentRedefinition();
	
	default boolean isRedefinitionOf(InterpretedMemberRedefinition other) {
		InterpretedMemberRedefinition r = this;
		do {
			if (r.equalsMember(other))
				return true;
			r = r.parentRedefinition();
		} while (r != null);
		return false;
	}
	
	InterpretedMemberDefinition definition();
	
	InterpretedMemberUse getUse(@Nullable InterpretedTypeUse targetType, Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments);
	
	boolean equalsMember(InterpretedMemberRedefinition other);
	
}
