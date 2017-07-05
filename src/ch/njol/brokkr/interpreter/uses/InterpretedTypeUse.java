package ch.njol.brokkr.interpreter.uses;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

/**
 * A type use is any use of a type apart from its own definition.
 * <p>
 * This is necessarily also an object, as all type uses can be put into (type) tuples which are themselves objects.
 */
public interface InterpretedTypeUse extends InterpretedObject {
	
	public boolean equalsType(InterpretedTypeUse other);
	
	public boolean isSubtypeOfOrEqual(InterpretedTypeUse other);
	
	public boolean isSupertypeOfOrEqual(InterpretedTypeUse other);
	
	public List<? extends InterpretedMemberUse> members();
	
	@Override
	default @NonNull InterpretedClassUse nativeClass() {
		return new InterpretedTypeUseClassUse(this);
	}
	
	public default @Nullable InterpretedMemberUse getMemberByName(final String name) {
		for (final InterpretedMemberUse m : members()) {
			if (m.redefinition().name().equals(name)) {
				return m;
			}
		}
		return null;
	}
	
	public default @Nullable InterpretedMemberUse getMember(final InterpretedMemberDefinition definition) {
		for (final InterpretedMemberUse m : members()) {
			if (m.definition().equalsMember(definition)) {
				return m;
			}
		}
		return null;
	}
	
	public default @Nullable InterpretedGenericTypeUse getGenericTypeByName(final String name) {
		final InterpretedMemberUse member = getMemberByName(name);
		return member instanceof InterpretedGenericTypeUse ? (InterpretedGenericTypeUse) member : null;
	}
	
	public default InterpretedGenericTypeUse getGenericType(final InterpretedGenericTypeDefinition definition) {
		final InterpretedMemberUse member = getMember(definition);
		if (member == null)
			throw new InterpreterException("Missing generic type " + definition + " in " + this);
		return (InterpretedGenericTypeUse) member;
	}
	
	public default @Nullable InterpretedAttributeUse getAttributeByName(final String name) {
		final InterpretedMemberUse member = getMemberByName(name);
		return member instanceof InterpretedAttributeUse ? (InterpretedAttributeUse) member : null;
	}
	
}
