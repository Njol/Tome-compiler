package ch.njol.tome.ir.definitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRUnknownAttributeDefinition;
import ch.njol.tome.ir.IRContext.IRUnresolvedTypeDefinition;
import ch.njol.tome.ir.nativetypes.IRBrokkrTypeClassDefinition;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleDefinition;
import ch.njol.tome.ir.nativetypes.internal.IRNativeTypeClassDefinition;
import ch.njol.tome.ir.uses.IRGenericTypeUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRTypeUseClassUse.IRTypeUseClassDefinition;

/**
 * The intermediate representation of a type.
 * <p>
 * May be the description of a Brokkr type ({@link IRBrokkrClassDefinition}, {@link IRBrokkrInterfaceDefinition}), a tuple type ({@link IRTypeTupleDefinition})
 * or a native type ({@link IRNativeTypeClassDefinition}).
 */
public interface IRTypeDefinition extends IRElement, Comparable<IRTypeDefinition> {
	
	// TODO rename
	// FIXME is this even useful?
//	IRClassDefinition nativeClass();
	
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
	 * Gets an attribute by the name it has in this type. Also finds inherited attributes.
	 */
	default @Nullable IRAttributeRedefinition getAttributeByName(final String name) {
		final IRMemberRedefinition m = getMemberByName(name);
		if (m instanceof IRAttributeRedefinition)
			return (IRAttributeRedefinition) m;
		return null;
	}
	
	/**
	 * Gets an attribute by the name it has in this type. Also finds inherited attributes.
	 */
	default IRAttributeRedefinition getAttributeByName(final String name, final  @Nullable ASTElementPart location) {
		final IRMemberRedefinition m = getMemberByName(name);
		if (m instanceof IRAttributeRedefinition)
			return (IRAttributeRedefinition) m;
		return new IRUnknownAttributeDefinition(this, name, "There's no attibute named '" + name + "' in '" + this + "'", location, getIRContext());
	}
	
//	@Nullable
//	IRAttributeRedefinition getAttributeRedefinition(IRAttributeDefinition definition);
	
	public Set<? extends IRTypeUse> allInterfaces();
	
	public List<IRAttributeRedefinition> positionalGenericParameters();
	
	public boolean equalsType(IRTypeDefinition other);
	
	int typeHashCode();
	
	/**
	 * Imposes a total order on type definitions. This order is used for unique representations of "and" and "or" types, and may thus be arbitrary (as long as it is consistent).
	 * <p>
	 * It is defined as follows: Ties for the same IRTypeDefinition types (and type, tuple type, simple type, ...) are broken with either comparing contained types,
	 * or for simple types by comparing "type module + name" (using the original name), or some other method for uniquely ordering instances of the IRTypeDefinition.
	 * <p>
	 * For different types, the order is according to {@link #compareTypeDefinitionClasses(Class, Class)}.
	 */
	@Override
	int compareTo(IRTypeDefinition other);
	
	/**
	 * For internal use only.
	 */
	final static List<Class<? extends IRTypeDefinition>> TYPE_DEFINITION_CLASS_ORDER = Collections.unmodifiableList(Arrays.asList(
			IRUnknownTypeDefinition.class, IRUnresolvedTypeDefinition.class, IRBrokkrInterfaceDefinition.class, IRBrokkrTypeClassDefinition.class, IRTypeUseClassDefinition.class, IRNativeTypeClassDefinition.class));
	
	/**
	 * Used to uniquely order different IRTypeDefinition classes. Must not be called with objects of the same type - use the following as the implementation of
	 * {@link #compareTo(IRTypeDefinition)}:
	 * 
	 * <pre>
	 * if (other instanceof ...TypeDefinition) {
	 *     return ...;
	 * }
	 * return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
	 * </pre>
	 * <p>
	 * The current order is: unknown type &lt; unresolved type &lt; Brokkr type &lt; type class &lt; type use class &lt; native type
	 * 
	 * @param thisClass Class of the object {@link #compareTo(IRTypeDefinition)} is called on
	 * @param otherClass Class of the argument of {@link #compareTo(IRTypeDefinition)}
	 * @return An integer representing the ordering of IRTypeDefinition classes ready to be returned by {@link #compareTo(IRTypeDefinition)}.
	 */
	public static int compareTypeDefinitionClasses(Class<? extends IRTypeDefinition> thisClass, Class<? extends IRTypeDefinition> otherClass) {
		if (thisClass == IRBrokkrClassDefinition.class)
			thisClass = IRBrokkrInterfaceDefinition.class;
		if (otherClass == IRBrokkrClassDefinition.class)
			otherClass = IRBrokkrInterfaceDefinition.class;
		final int thisIndex = TYPE_DEFINITION_CLASS_ORDER.indexOf(thisClass), otherIndex = TYPE_DEFINITION_CLASS_ORDER.indexOf(otherClass);
		assert thisIndex >= 0 && otherIndex >= 0 && thisIndex != otherIndex : thisClass + ", " + otherClass;
		return thisIndex - otherIndex;
	}
	
	/*
	public boolean isSubtypeOfOrEqual(IRTypeDefinition other);
	
	public boolean isSupertypeOfOrEqual(IRTypeDefinition other);
	*/
	
	public default IRTypeUse getUse() {
		return new IRSimpleTypeUse(this);
	}
	
	public default IRTypeUse getGenericUse(final Map<IRAttributeDefinition, IRGenericArgument> genericArguments) {
		return new IRGenericTypeUse(getUse(), genericArguments);
	}
	
	// utility methods for getting known generic type uses with 0-3 type arguments (for more, use the generic getUse above)
	
	public default IRTypeUse getGenericUse(final String genericTypeName, final IRGenericArgument genericTypeValue, final @Nullable ASTElementPart location) {
		return getGenericUse(Collections.singletonMap(getAttributeByName(genericTypeName, location).definition(), genericTypeValue));
	}
	
	public default IRTypeUse getGenericUse(
			final String genericTypeName1, final IRGenericArgument genericTypeValue1,
			final String genericTypeName2, final IRGenericArgument genericTypeValue2,
			final @Nullable ASTElementPart location) {
		final Map<IRAttributeDefinition, IRGenericArgument> map = new HashMap<>();
		map.put(getAttributeByName(genericTypeName1, location).definition(), genericTypeValue1);
		map.put(getAttributeByName(genericTypeName2, location).definition(), genericTypeValue2);
		return getGenericUse(map);
	}
	
	public default IRTypeUse getGenericUse(
			final String genericTypeName1, final IRGenericArgument genericTypeValue1,
			final String genericTypeName2, final IRGenericArgument genericTypeValue2,
			final String genericTypeName3, final IRGenericArgument genericTypeValue3,
			final @Nullable ASTElementPart location) {
		final Map<IRAttributeDefinition, IRGenericArgument> map = new HashMap<>();
		map.put(getAttributeByName(genericTypeName1, location).definition(), genericTypeValue1);
		map.put(getAttributeByName(genericTypeName2, location).definition(), genericTypeValue2);
		map.put(getAttributeByName(genericTypeName3, location).definition(), genericTypeValue3);
		return getGenericUse(map);
	}
	
}
