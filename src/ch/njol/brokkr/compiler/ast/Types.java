package ch.njol.brokkr.compiler.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.MemberContainer;
import ch.njol.brokkr.compiler.ast.Interfaces.Named;
import ch.njol.brokkr.compiler.ast.Interfaces.NamedElement;
import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Types.GenericParameter;
import ch.njol.brokkr.interpreter.InterpretedMember;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeType;
import ch.njol.util.StringUtils;

public class Types {
	
	/*
	 * type declarations and uses:
	 * - type declaration (interface/class {...})
	 * - generic param declaration (in types or methods)
	 * - normal type use (method param type, result type, var type)
	 * - use as value (var x = SomeType;)
	 * - in generics: use (T in X<T>) and constraints (T in X<Y extends T>)
	 * - implicit, e.g. 'x ? y : z' has type 'Y | Z'
	 */
	
	// ========================================= interfaces =========================================
	
	/**
	 * Any kind of type, which may be a type declaration ('interface X {...}'/'interface x Y {...}'), a generic type declaration ('type T'/'&lt;T>') or use ('x: T'/'x = T'),
	 * an implicit type ('c ? t : f' has type 'T | F'), or a used type ('x: List&lt;String>'/'x = Any').
	 */
//	public static interface Type extends MemberContainer {
//		
//		@Override
//		String toString();
//		
//		List<? extends Type> parentTypes();
//		
//		@Override
//		default List<? extends Type> parentMemberContainers() {
//			return parentTypes();
//		}
//		
//		/**
//		 * @param other
//		 * @return Whether this type represents exactly the same type as 'other'.
//		 */
//		boolean equalsType(final Type other);
//		
//		/**
//		 * @param other
//		 * @return Whether this type is a subtype of 'other'
//		 */
//		boolean isSubTypeOfOrEqual(final Type other);
//		
//		/**
//		 * @param other
//		 * @return Whether this type is a supertype of 'other'
//		 */
//		default boolean isSuperTypeOfOrEqual(final Type other) {
//			return other.isSubTypeOfOrEqual(this);
//		}
//		
//	}
	
	/**
	 * A simple type use like 'String' or 'T'.
	 */
	public interface SimpleTypeUse extends TypeUse {
		
		public @Nullable TypeDeclaration typeDeclaration();
		
		@SuppressWarnings("null")
		@Override
		default List<? extends Type> parentTypes() {
			final TypeDeclaration type = typeDeclaration();
			return type == null ? Collections.EMPTY_LIST : Arrays.asList(typeDeclaration());
		}
		
		@Override
		default boolean equalsType(final Type other) {
			if (!(other instanceof SimpleTypeUse))
				return false;
			final TypeDeclaration d1 = typeDeclaration(), d2 = ((SimpleTypeUse) other).typeDeclaration();
			return d1 != null && d2 != null && d1.equalsType(d2);
		}
		
		@Override
		default boolean isSubTypeOfOrEqual(final Type other) {
			final TypeDeclaration d = typeDeclaration();
			return d != null && d.isSubTypeOfOrEqual(other);
		}
		
	}
	
	/**
	 * A Type use with generic arguments, e.g. 'List&lt;String>'.
	 */
	public interface GenericTypeUse extends TypeUse {
		
		/**
		 * @return The base type of this generic type, e.g. 'List' for 'List&lt;String>'.
		 */
		public @Nullable TypeUse baseType();
		
		/**
		 * @return The generic arguments of this type use. The base type declaration should have matching generic parameters (which is checked by the semantic checker).
		 */
		List<? extends TypeUse> genericArguments();
		
		@Override
		default @Nullable Member getMember(final String name) {
			final TypeUse b = baseType();
			return b == null ? null : b.getMember(name);
		}
		
		@SuppressWarnings("null")
		@Override
		default List<? extends Type> parentTypes() {
			return Arrays.asList(baseType());
		}
		
		@SuppressWarnings("null")
		@Override
		default boolean equalsType(final Type other) {
			if (other instanceof GenericTypeUse) {
				final GenericTypeUse o = (GenericTypeUse) other;
				final List<? extends TypeUse> oga = o.genericArguments(), ga = genericArguments();
				if (!o.baseType().equalsType(other) || ga.size() != oga.size()) // TODO left-out generic arguments take default value (or not?)
					return false;
				for (int i = 0; i < ga.size(); i++) {
					if (!ga.get(i).equalsType(oga.get(i)))
						return false;
				}
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		default boolean isSubTypeOfOrEqual(final Type other) {
			// TODO
//			if (other instanceof GenericTypeUse) {
//				final GenericTypeUse o = (GenericTypeUse) other;
//
//				if (o.baseType().equalsType(other)) {
//
//				}
//			} else {
			return false;
//			}
		}
		
	}
	
	public interface TupleTypeUse extends TypeUse {
		
		List<? extends TupleTypeUseEntry> entries();
		
		@Override
		default List<? extends Member> declaredMembers() {
			return entries();
		}
		
		@SuppressWarnings("null")
		@Override
		default boolean equalsType(final Type other) {
			if (!(other instanceof TupleTypeUse))
				return false;
			final List<? extends TupleTypeUseEntry> e1 = entries(), e2 = ((TupleTypeUse) other).entries();
			if (e1.size() != e2.size())
				return false;
			for (int i = 0; i < e1.size(); i++) {
				if (!e1.get(i).equalsTupleTypeUseEntry(e2.get(i)))
					return false;
			}
			return true;
		}
		
		@Override
		default boolean isSubTypeOfOrEqual(final Type other) {
			// TODO define tuple subtyping rules
			return false;
		}
		
	}
	
	public interface TupleTypeUseEntry extends Member, Named {
		
		public @Nullable TypeUse type();
		
		@Override
		default boolean isInherited() {
			return true; // TODO make proper subtype relationships between tuples
		}
		
		default boolean equalsTupleTypeUseEntry(final TupleTypeUseEntry other) {
			final String n1 = name(), n2 = other.name();
			final TypeUse t1 = type(), t2 = other.type();
			return n1 != null && n1.equals(n2) && t1 != null && t2 != null && t1.equalsType(t2);
		}
		
	}
	
	public interface OrTypeUse extends TypeUse {
		
		List<? extends TypeUse> orTypes();
		
		@SuppressWarnings("null")
		@Override
		default List<? extends Type> parentTypes() {
			return Collections.EMPTY_LIST; // FIXME calculate supertype(s)
		}
		
		@Override
		default boolean equalsType(final Type other) {
			if (!(other instanceof OrTypeUse))
				return false;
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		default boolean isSubTypeOfOrEqual(final Type other) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public interface AndTypeUse extends TypeUse {
		
		List<? extends TypeUse> andTypes();
		
		@Override
		default List<? extends Type> parentTypes() {
			return andTypes();
		}
		
		@Override
		default boolean equalsType(final Type other) {
			if (!(other instanceof AndTypeUse))
				return false;
			// FIXME this is only correct for specific and types - nested ones or redundant ones are not properly handled here
			final List<? extends TypeUse> tt = andTypes(), ot = ((AndTypeUse) other).andTypes();
			final Comparator<Type> c = (t1, t2) -> System.identityHashCode(t1) - System.identityHashCode(t2);
			Collections.sort(tt, c);
			Collections.sort(ot, c);
			return tt.equals(ot);
		}
		
		@Override
		default boolean isSubTypeOfOrEqual(final Type other) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	// ========================================= simple classes =========================================
	
	public final static class NullTypeUse implements TypeUse {
		@SuppressWarnings("null")
		@Override
		public List<? extends Type> parentTypes() {
			return Collections.EMPTY_LIST; // no explicit parents
		}
		
		@Override
		public boolean equalsType(final Type other) {
			return other instanceof NullTypeUse;
		}
		
		@Override
		public boolean isSubTypeOfOrEqual(final Type other) {
			return true; // null is of every type // FIXME actually only all 'nullable' types!
		}
		
		@Override
		public String toString() {
			return "Null";
		}
	}
	
	public final static class SimpleTypeUseImpl implements SimpleTypeUse {
		
		private final TypeDeclaration typeDeclaration;
		
		public SimpleTypeUseImpl(final TypeDeclaration typeDeclaration) {
			this.typeDeclaration = typeDeclaration;
		}
		
		@Override
		public TypeDeclaration typeDeclaration() {
			return typeDeclaration;
		}
		
		@Override
		public String toString() {
			return "" + typeDeclaration;
		}
	}
	
	public final static class GenericTypeUseImpl implements GenericTypeUse {
		private final TypeUse baseType;
		private final List<TypeUse> genericArguments;
		
		@SuppressWarnings("null")
		public GenericTypeUseImpl(final TypeUse baseType, final TypeUse... genericArguments) {
			this.baseType = baseType;
			this.genericArguments = Arrays.asList(genericArguments);
		}
		
		@Override
		public TypeUse baseType() {
			return baseType;
		}
		
		@Override
		public List<TypeUse> genericArguments() {
			return genericArguments;
		}
		
		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			b.append(baseType);
			b.append('<');
			boolean first = true;
			for (final TypeUse ga : genericArguments()) {
				if (!first)
					b.append(", ");
				else
					first = false;
				b.append(ga);
			}
			b.append('>');
			return "" + b;
		}
	}
	
	public final static class TupleTypeUseImpl implements TupleTypeUse {
		private final TypeTupleUseEntryImpl[] entries;
		
		public TupleTypeUseImpl(final TypeTupleUseEntryImpl[] entries) {
			this.entries = entries;
		}
		
		@SuppressWarnings("null")
		public TupleTypeUseImpl(final Stream<TypeTupleUseEntryImpl> entries) {
			this(entries.toArray(i -> new TypeTupleUseEntryImpl[i]));
		}
		
		@Override
		public @Nullable Member getMember(final String name) {
			for (int i = 0; i < entries.length; i++) {
				if (name.equals(entries[i].name))
					return entries[i];
			}
			return null;
		}
		
		@SuppressWarnings("null")
		@Override
		public List<? extends TupleTypeUseEntry> entries() {
			return Arrays.asList(entries);
		}
		
		@SuppressWarnings("null")
		@Override
		public List<? extends Type> parentTypes() {
//			final TypeDeclaration tuple = getStandardType("lang", "Tuple");
//			return tuple == null ? Collections.EMPTY_LIST : Arrays.asList(tuple);
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0; i < entries.length; i++) {
				if (i != 0)
					b.append(", ");
				b.append(entries[i].type);
			}
			b.append(']');
			return "" + b;
		}
	}
	
	public static class TypeTupleUseEntryImpl implements TupleTypeUseEntry {
		public TypeUse type;
		public @Nullable String name;
		
		public TypeTupleUseEntryImpl(final TypeUse type, @Nullable final String name) {
			this.type = type;
			this.name = name;
		}
		
		@Override
		public @Nullable String name() {
			return name;
		}
		
		@Override
		public @Nullable TypeUse type() {
			return type;
		}

		@Override
		public @NonNull InterpretedMember interpreted() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public final static class OrTypeUseImpl implements OrTypeUse {
		private final TypeUse[] types;
		
		public OrTypeUseImpl(final TypeUse... types) {
			this.types = types;
		}
		
		@SuppressWarnings("null")
		@Override
		public String toString() {
			return "(" + StringUtils.join(types, " | ") + ")";
		}
		
		@SuppressWarnings("null")
		@Override
		public List<? extends TypeUse> orTypes() {
			return Arrays.asList(types);
		}
	}
	
	public final static class AndTypeUseImpl implements AndTypeUse {
		private final List<? extends TypeUse> types;
		
		@SuppressWarnings("null")
		public AndTypeUseImpl(final TypeUse... types) {
			this.types = Arrays.asList(types);
		}
		
		public AndTypeUseImpl(final List<? extends TypeUse> types) {
			this.types = types;
		}
		
		@Override
		public String toString() {
			return "(" + StringUtils.join(types, " & ") + ")";
		}
		
		@Override
		public List<? extends TypeUse> andTypes() {
			return types;
		}
	}
	
}
