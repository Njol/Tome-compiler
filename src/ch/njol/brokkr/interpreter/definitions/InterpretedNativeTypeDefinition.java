package ch.njol.brokkr.interpreter.definitions;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrInterface;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedSimpleNativeTypeClass;
import ch.njol.brokkr.interpreter.nativetypes.internal.InterpretedNativeSimpleNativeClass;
import ch.njol.brokkr.interpreter.uses.InterpretedAndTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedOrTypeUse;

/**
 * The native description of a type.
 * <p>
 * May be the description of a Brokkr type ({@link InterpretedNativeBrokkrClass}, {@link InterpretedNativeBrokkrInterface}),
 * a native type ({@link InterpretedNativeSimpleNativeClass}), a tuple type ({@link InterpretedNativeTupleType}),
 * or a combined type ({@link InterpretedOrTypeUse}, {@link InterpretedAndTypeUse}).
 */
public interface InterpretedNativeTypeDefinition {
	
	// this is just a default implementation, which may be overridden (as e.g. tuples do)
	default InterpretedNativeClassDefinition nativeClass() {
		return new InterpretedSimpleNativeTypeClass(this);
	}
	
	List<? extends InterpretedMemberRedefinition> members();
	
	/**
	 * @param name
	 * @return A member of this type that has the given name in this type, or null if no member with this name exists.
	 */
	default @Nullable InterpretedMemberRedefinition getMemberByName(String name) {
		for (InterpretedMemberRedefinition m : members()) {
			if (name.equals(m.name()))
				return m;
		}
		return null;
	}

	default @Nullable InterpretedMemberRedefinition getMember(InterpretedMemberDefinition definition) {
		for (InterpretedMemberRedefinition m : members()) {
			if (definition.equals(m.definition()))
				return m;
		}
		return null;
	}

	/**
	 * Gets an attribute by the name it has in this type.
	 */
	default @Nullable InterpretedAttributeRedefinition getAttributeByName(final String name) {
		final InterpretedMemberRedefinition m = getMemberByName(name);
		if (m instanceof InterpretedAttributeRedefinition)
			return (InterpretedAttributeRedefinition) m;
		return null;
	}

	default @Nullable InterpretedGenericTypeRedefinition getGenericTypeByName(final String name) {
		final InterpretedMemberRedefinition m = getMemberByName(name);
		if (m instanceof InterpretedGenericTypeRedefinition)
			return (InterpretedGenericTypeRedefinition) m;
		return null;
	}
	
//	@Nullable
//	InterpretedAttributeRedefinition getAttributeRedefinition(InterpretedAttributeDefinition definition);
	
	public boolean equalsType(InterpretedNativeTypeDefinition other);
	
	public boolean isSubtypeOfOrEqual(InterpretedNativeTypeDefinition other);
	
	public boolean isSupertypeOfOrEqual(InterpretedNativeTypeDefinition other);

}
