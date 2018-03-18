package ch.njol.brokkr.ir.uses;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;

/**
 * A type use is any use of a type apart from its own definition.
 */
public interface IRTypeUse extends IRExpression, Comparable<IRTypeUse> {
	
	/**
	 * @return A set of all interfaces implemented or extended by this type. Includes this type itself if this is an interface.
	 *         // TODO include classes too? // TODO ALL interfaces? or only directly implemented/extended ones?
	 */
	public Set<? extends IRTypeUse> allInterfaces();
	
	public int typeHashCode();
	
	public boolean equalsType(IRTypeUse other);
	
	/**
	 * Imposes a total order on type uses. This order is used for unique representations of "and" and "or" types, and may thus be arbitrary (as long as it is consistent).
	 * <p>
	 * It is defined as follows: Ties for the same IRTypeUse types (and type, tuple type, simple type, ...) are broken with either comparing contained types,
	 * or for simple types by comparing "type module + name" (using the original name), or some other method for uniquely ordering instances of the IRTypeUse.
	 * <p>
	 * For different types, the order is according to {@link #compareTypeUseClasses(Class, Class)}.
	 */
	@Override
	int compareTo(IRTypeUse other);
	
	/**
	 * For internal use only.
	 */
	final static List<Class<? extends IRTypeUse>> TYPE_USE_CLASS_ORDER = Collections.unmodifiableList(Arrays.asList(
			IRUnknownTypeUse.class, IRGenericTypeAccess.class, IRGenericTypeUse.class, IRSimpleTypeUse.class, IRTypeUseClassUse.class, IRSelfTypeUse.class, IRTypeTuple.class, IRAndTypeUse.class, IROrTypeUse.class));
	
	/**
	 * Used to uniquely order different IRTypeUse classes. Must not be called with objects of the same type - use the following as the implementation of
	 * {@link #compareTo(IRTypeUse)}:
	 * 
	 * <pre>
	 * if (other instanceof ...TypeUse) {
	 *     return ...;
	 * }
	 * return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	 * </pre>
	 * <p>
	 * The current order is: unknown type &lt; generic type access (A.B) &lt; generic type &lt; simple type &lt; Self &lt; tuple type &lt; and type &lt; or type
	 * 
	 * @param thisClass Class of the object {@link #compareTo(IRTypeUse)} is called on
	 * @param otherClass Class of the argument of {@link #compareTo(IRTypeUse)}
	 * @return An integer representing the ordering of IRTypeUse classes ready to be returned by {@link #compareTo(IRTypeUse)}.
	 */
	public static int compareTypeUseClasses(Class<? extends IRTypeUse> thisClass, Class<? extends IRTypeUse> otherClass) {
		if (thisClass == IRSimpleClassUse.class)
			thisClass = IRSimpleTypeUse.class;
		if (otherClass == IRSimpleClassUse.class)
			otherClass = IRSimpleTypeUse.class;
		final int thisIndex = TYPE_USE_CLASS_ORDER.indexOf(thisClass), otherIndex = TYPE_USE_CLASS_ORDER.indexOf(otherClass);
		assert thisIndex >= 0 && otherIndex >= 0 && thisIndex != otherIndex : thisClass + ", " + otherClass;
		return thisIndex - otherIndex;
	}
	
	public boolean isSubtypeOfOrEqual(IRTypeUse other);
	
	public boolean isSupertypeOfOrEqual(IRTypeUse other);
	
	public List<? extends IRMemberUse> members();
	
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
	
	public default @Nullable IRGenericTypeUse getGenericType(final IRGenericTypeDefinition definition) {
		final IRMemberUse member = getMember(definition);
		if (member == null || !(member instanceof IRGenericTypeUse))
			return null; //throw new InterpreterException("Missing generic type " + definition + " in " + this); // TODO wrong exception type -- and maybe make this not an exception at all
		return (IRGenericTypeUse) member;
	}
	
	public default @Nullable IRAttributeUse getAttributeByName(final String name) {
		final IRMemberUse member = getMemberByName(name);
		return member instanceof IRAttributeUse ? (IRAttributeUse) member : null;
	}
	
	@Override
	public @NonNull InterpretedTypeUse interpret(InterpreterContext context) throws InterpreterException;
	
}
