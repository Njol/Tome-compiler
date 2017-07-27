package ch.njol.brokkr.ir.uses;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;

/**
 * A type use is any use of a type apart from its own definition.
 * <p>
 * This is necessarily also an object, as all type uses can be put into (type) tuples which are themselves objects. TODO could also create a special type object/class
 */
public interface IRTypeUse extends InterpretedObject {
	
	public boolean equalsType(IRTypeUse other);
	
	public boolean isSubtypeOfOrEqual(IRTypeUse other);
	
	public boolean isSupertypeOfOrEqual(IRTypeUse other);
	
	public List<? extends IRMemberUse> members();
	
	@Override
	default @NonNull IRClassUse nativeClass() {
		return new IRTypeUseClassUse(this);
	}
	
	public default @Nullable IRMemberUse getMemberByName(final String name) {
		for (final IRMemberUse m : members()) {
			if (m.redefinition().name().equals(name)) {
				return m;
			}
		}
		return null;
	}
	
	public default @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		for (final IRMemberUse m : members()) {
			if (m.definition().equalsMember(definition)) {
				return m;
			}
		}
		return null;
	}
	
	public default @Nullable IRGenericTypeUse getGenericTypeByName(final String name) {
		final IRMemberUse member = getMemberByName(name);
		return member instanceof IRGenericTypeUse ? (IRGenericTypeUse) member : null;
	}
	
	public default IRGenericTypeUse getGenericType(final IRGenericTypeDefinition definition) {
		final IRMemberUse member = getMember(definition);
		if (member == null)
			throw new InterpreterException("Missing generic type " + definition + " in " + this);
		return (IRGenericTypeUse) member;
	}
	
	public default @Nullable IRAttributeUse getAttributeByName(final String name) {
		final IRMemberUse member = getMemberByName(name);
		return member instanceof IRAttributeUse ? (IRAttributeUse) member : null;
	}
	
}
