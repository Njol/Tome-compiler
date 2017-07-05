package ch.njol.brokkr.interpreter.uses;

import java.util.List;

import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeRedefinition;

public class InterpretedGenericTypeUse implements InterpretedMemberUse, InterpretedTypeUse {
	
	private final InterpretedGenericTypeRedefinition redefinition;
	private final InterpretedTypeUse typeUse;
	
	public InterpretedGenericTypeUse(final InterpretedGenericTypeRedefinition redefinition, final InterpretedTypeUse typeUse) {
		this.redefinition = redefinition;
		this.typeUse = typeUse;
	}
	
	@Override
	public InterpretedGenericTypeRedefinition redefinition() {
		return redefinition;
	}
	
	@Override
	public InterpretedGenericTypeDefinition definition() {
		return redefinition.definition();
	}
	
	@Override
	public InterpretedClassUse nativeClass() {
		return typeUse.nativeClass();
	}
	
	@Override
	public boolean equalsType(final InterpretedTypeUse other) {
		return typeUse.equalsType(other);
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final InterpretedTypeUse other) {
		return typeUse.isSubtypeOfOrEqual(other);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final InterpretedTypeUse other) {
		return typeUse.isSupertypeOfOrEqual(other);
	}
	
	@Override
	public List<? extends InterpretedMemberUse> members() {
		return typeUse.members();
	}
	
}
