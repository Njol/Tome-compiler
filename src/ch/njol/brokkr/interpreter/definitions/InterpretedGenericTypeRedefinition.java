package ch.njol.brokkr.interpreter.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.uses.InterpretedGenericTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public interface InterpretedGenericTypeRedefinition extends InterpretedMemberRedefinition {
	
	@Override
	InterpretedGenericTypeDefinition definition();
	
	@Override
	@Nullable
	InterpretedGenericTypeRedefinition parentRedefinition();
	
	@Override
	default InterpretedMemberUse getUse(@Nullable final InterpretedTypeUse targetType, final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments) {
		final InterpretedTypeUse type = genericArguments.get(this);
		if (type == null)
			throw new InterpreterException("Unset generic argument " + this);
		return new InterpretedGenericTypeUse(this, type);
	}
	
}
