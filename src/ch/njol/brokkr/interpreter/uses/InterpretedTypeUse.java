package ch.njol.brokkr.interpreter.uses;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A type use is any use of a type apart from its own definition.
 */
public interface InterpretedTypeUse {
	
	public boolean equalsType(InterpretedTypeUse other);
	
	public boolean isSubtypeOfOrEqual(InterpretedTypeUse other);
	
	public boolean isSupertypeOfOrEqual(InterpretedTypeUse other);

	public @Nullable InterpretedMemberUse getMemberByName(String name);
	
	public InterpretedTypeUse typeType();
	
}
