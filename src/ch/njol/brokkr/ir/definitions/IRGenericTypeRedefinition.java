package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.uses.IRGenericTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRGenericTypeRedefinition extends IRMemberRedefinition, IRTypeDefinitionOrGenericTypeRedefinition {
	
	@Override
	IRGenericTypeDefinition definition();
	
	@Override
	@Nullable
	IRGenericTypeRedefinition parentRedefinition();
	
	@Override
	default boolean isStatic() {
		return true;
	}
	
	@Override
	default @NonNull IRGenericTypeUse getUse(@Nullable final IRTypeUse targetType, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		return new IRGenericTypeUse(this, genericArguments.get(this));
	}
	
	@Override
	default IRGenericTypeUse getUse(final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		return getUse(null, genericArguments);
	}
	
	IRTypeUse upperBound();
	
}
