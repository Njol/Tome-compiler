package ch.njol.brokkr.ir.uses;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;

/**
 * The use of a generic type, with or without known the exact type
 */
public class IRGenericTypeUse implements IRMemberUse, IRTypeUse {
	
	private final IRGenericTypeRedefinition redefinition;
	private final @Nullable IRTypeUse typeUse;
	
	public IRGenericTypeUse(final IRGenericTypeRedefinition redefinition, final @Nullable IRTypeUse typeUse) {
		this.redefinition = redefinition;
		this.typeUse = typeUse;
	}
	
	@Override
	public IRGenericTypeRedefinition redefinition() {
		return redefinition;
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		return redefinition.definition();
	}
	
	@Override
	public IRClassUse nativeClass() {
		return (typeUse != null ? typeUse : redefinition.upperBound()).nativeClass();
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		// TODO wrong
		return (typeUse != null ? typeUse : redefinition.upperBound()).equalsType(other);
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		return (typeUse != null ? typeUse : redefinition.upperBound()).isSubtypeOfOrEqual(other);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		return (typeUse != null ? typeUse : redefinition.upperBound()).isSupertypeOfOrEqual(other);
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return (typeUse != null ? typeUse : redefinition.upperBound()).members();
	}
	
	@Override
	public String toString() {
		return typeUse != null ? typeUse.toString() : redefinition.name();
	}
	
}
