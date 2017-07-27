package ch.njol.brokkr.ir.definitions;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.nativetypes.IRSimpleNativeTypeClass;
import ch.njol.brokkr.ir.nativetypes.internal.IRSimpleNativeClass;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

/**
 * The intermediate representation of a type.
 * <p>
 * May be the description of a Brokkr type ({@link IRBrokkrClass}, {@link IRBrokkrInterface}),
 * or a native type ({@link IRSimpleNativeClass}).
 */
public interface IRTypeDefinition extends IRTypeDefinitionOrGenericTypeRedefinition {
	
	// this is just a default implementation, which may be overridden (as e.g. tuples do)
	default IRClassDefinition nativeClass() {
		return new IRSimpleNativeTypeClass(this);
	}
	
	/**
	 * @return A list of all members of this type, including inherited ones.
	 */
	List<? extends IRMemberRedefinition> members();
	
	/**
	 * @param name
	 * @return A member of this type whose name in this type equals the given string, or null if no member with this name exists.
	 */
	default @Nullable IRMemberRedefinition getMemberByName(final String name) {
		for (final IRMemberRedefinition m : members()) {
			if (name.equals(m.name()))
				return m;
		}
		return null;
	}
	
	default @Nullable IRMemberRedefinition getMember(final IRMemberDefinition definition) {
		for (final IRMemberRedefinition m : members()) {
			if (definition.equals(m.definition()))
				return m;
		}
		return null;
	}
	
	/**
	 * Gets an attribute by the name it has in this type.
	 */
	default @Nullable IRAttributeRedefinition getAttributeByName(final String name) {
		final IRMemberRedefinition m = getMemberByName(name);
		if (m instanceof IRAttributeRedefinition)
			return (IRAttributeRedefinition) m;
		return null;
	}
	
	default @Nullable IRGenericTypeRedefinition getGenericTypeByName(final String name) {
		final IRMemberRedefinition m = getMemberByName(name);
		if (m instanceof IRGenericTypeRedefinition)
			return (IRGenericTypeRedefinition) m;
		return null;
	}
	
//	@Nullable
//	IRAttributeRedefinition getAttributeRedefinition(IRAttributeDefinition definition);
	
	public boolean equalsType(IRTypeDefinition other);
	
	public boolean isSubtypeOfOrEqual(IRTypeDefinition other);
	
	public boolean isSupertypeOfOrEqual(IRTypeDefinition other);
	
	int typeHashCode();
	
	@Override
	public default IRTypeUse getUse(final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		return new IRSimpleTypeUse(this, genericArguments);
	}
	
}
