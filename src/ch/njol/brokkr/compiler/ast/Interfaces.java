package ch.njol.brokkr.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Members.MemberModifiers;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableRedefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedNativeTupleValueAndEntry;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedTypeTuple;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeObject;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class Interfaces {
	
	public static interface Named {
		
		public @Nullable String name();
		
	}
	
	public static interface NamedElement extends Element, Named {
		
		@Nullable
		WordToken nameToken();
		
		@Override
		public default @Nullable String name() {
			final WordToken t = nameToken();
			return t == null ? null : t.word;
		}
		
		@Override
		public default int linkStart() {
			final WordToken t = nameToken();
			return t == null ? regionStart() : t.regionStart();
		}
		
		@Override
		public default int linkEnd() {
			final WordToken t = nameToken();
			return t == null ? regionEnd() : t.regionEnd();
		}
	}
	
	public static interface TypedElement extends Element {
		public InterpretedTypeUse interpretedType();
	}
	
	public static interface FormalVariableOrAttribute extends NamedElement, TypedElement {}
	
	public static interface FormalVariable extends FormalVariableOrAttribute {
		public InterpretedVariableRedefinition interpreted();
	}
	
	public static interface FormalParameter extends FormalVariable {
		@Override
		InterpretedParameterRedefinition interpreted();
		
//		public @Nullable FormalParameter overridden();
	}
	
	public static interface FormalResult extends TypedElement, NamedElement {
		
		InterpretedResultRedefinition interpreted();
		
	}
	
	public static interface FormalAttribute extends FormalVariableOrAttribute, HasVariables {
		public MemberModifiers modifiers();
		
		public List<? extends FormalError> declaredErrors();
		
		public default @Nullable InterpretedError getError(final String name) {
			for (final FormalError e : declaredErrors()) {
				if (name.equals(e.name()))
					return e.interpreted();
			}
			final InterpretedMemberRedefinition parent = modifiers().overridden.get();
			return parent != null && parent instanceof InterpretedAttributeRedefinition ? ((InterpretedAttributeRedefinition) parent).getErrorByName(name) : null;
		}
		
		public List<? extends FormalResult> declaredResults();
		
		public default @Nullable InterpretedResultRedefinition getResult(final String name) {
			for (final FormalResult r : declaredResults()) {
				if (name.equals(r.name()))
					return r.interpreted();
			}
			final InterpretedMemberRedefinition parent = modifiers().overridden.get();
			if (parent != null && parent instanceof InterpretedAttributeRedefinition) {
				final InterpretedResultRedefinition r = ((InterpretedAttributeRedefinition) parent).getResultByName(name);
				if (r != null)
					return r;
			}
			if ("result".equals(name) && declaredResults().size() > 0) {
				@SuppressWarnings("null")
				final FormalResult first = declaredResults().get(0);
				if (first.name() == null)
					return first.interpreted();
			}
			return null;
		}
		
		@SuppressWarnings("null")
		public default List<InterpretedResultRedefinition> allResults() {
			final List<InterpretedResultRedefinition> allResults = new ArrayList<>();
			final InterpretedMemberRedefinition parent = modifiers().overridden.get();
			if (parent != null && parent instanceof InterpretedAttributeRedefinition)
				allResults.addAll(((InterpretedAttributeRedefinition) parent).results());
			outer: for (final FormalResult r : declaredResults()) {
				for (int i = 0; i < allResults.size(); i++) {
					if (Objects.equals(r.name(), allResults.get(i).name())) { // equal if the names are equal or if both are the default unnamed result (name() == null) // FIXME wrong, at least null != "result"
						allResults.set(i, r.interpreted());
						continue outer;
					}
				}
				allResults.add(r.interpreted());
			}
			return allResults;
		}
		
		/**
		 * @return The type of the main result of this method, or the type of this field. Null if this method returns nothing.
		 */
		@SuppressWarnings("null")
		@Override
		public default @Nullable InterpretedTypeUse interpretedType() {
			return getResult("result").type();
		}
		
		/**
		 * @return A tuple of all result types of this method (or a tuple with a single type if a field)
		 */
		@SuppressWarnings("null")
		public default InterpretedTypeTuple allTypes() {
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			final List<InterpretedResultRedefinition> results = allResults();
			for (int i = 0; i < results.size(); i++) {
				final InterpretedResultRedefinition r = results.get(i);
				final InterpretedTypeUse type = r.type();
				entries.add(new InterpretedNativeTupleValueAndEntry(i, type.typeType(), r.name(), type));
			}
			return new InterpretedTypeTuple(entries);
		}
		
		public InterpretedAttributeRedefinition interpreted();
		
	}
	
	public static interface HasVariables extends Element {

		public List<? extends InterpretedVariableRedefinition> allVariables();
		
		public default @Nullable InterpretedVariableRedefinition getVariableByName(final String name) {
			for (final InterpretedVariableRedefinition v : allVariables()) {
				if (name.equals(v.name()))
					return v;
			}
			return null;
		}
		
	}
	
	public static interface FormalError extends NamedElement/*, HasVariables*/ {
		
		InterpretedError interpreted();
		
	}
	
	public static interface Expression extends TypedElement {
		
		@Nullable
		InterpretedObject interpret(InterpreterContext context);
		
	}
	
	/**
	 * A type declaration, with members and possible supertypes (currently either an interface, class, generic type, enum, or enum constant declaration).
	 */
	public static interface TypeDeclaration extends /*MemberContainer,*/ NamedElement {
		
		List<? extends Member> declaredMembers();
		
		List<? extends GenericParameter> genericParameters();
		
		InterpretedTypeUse parentTypes();
		
		InterpretedNativeTypeDefinition interpreted();
		
	}
	
	public static interface GenericParameter {
		enum Variance {
			COVARIANT, CONTRAVARIANT, INVARIANT;
		}
		
		public Variance variance();
		
		public @Nullable InterpretedGenericTypeRedefinition declaration();
	}
	
	/**
	 * A type use, e.g. in 'x(String): Int' or 'var x = String'.
	 * It is also automatically a TypedElement whose type is Type&lt;<i>this</i>&gt;
	 */
	public static interface TypeUse extends /*MemberContainer,*/ TypedElement {
		
		@Override
		default InterpretedTypeUse interpretedType() {
			return staticallyKnownType();
		}
		
		InterpretedTypeUse staticallyKnownType();
		
		InterpretedTypeUse interpret(InterpreterContext context);
		
//		@SuppressWarnings("null")
//		@Override
//		default List<? extends MemberContainer> parentMemberContainers() {
//			return Collections.EMPTY_LIST;
//		}
	
	}
	
	public static interface TypeExpression extends TypeUse, Expression {
		
		@Override
		@NonNull
		InterpretedTypeObject interpret(InterpreterContext context);
		
	}
	
//	// FIXME
//	public static interface HasTypes {
//		public List<? extends TypeDeclaration> declaredTypes();
//
//		public default @Nullable TypeDeclaration getDeclaredType(final String name) {
//			for (final TypeDeclaration t : declaredTypes()) {
//				final WordToken n = t.nameToken();
//				if (n != null && name.equals(n.word))
//					return t;
//			}
//			return null;
//		}
//
//		public default @Nullable TypeDeclaration getType(final String name) {
//			TypeDeclaration t = getDeclaredType(name);
//			if (t != null)
//				return t;
//			for (final HasTypes ht : parentHasTypes()) {
//				assert ht != this : this;
//				t = ht.getType(name);
//				if (t != null)
//					return t;
//			}
//			return null;
//		}
//
//		public List<? extends HasTypes> parentHasTypes();
//	}

//	public static interface HasMembers extends Element {
//		public @Nullable Member getMember(final String name);
//
//		public default @Nullable FormalAttribute getAttribute(final String name) {
//			final Member m = getMember(name);
//			return m instanceof FormalAttribute ? (FormalAttribute) m : null;
//		}
//	}

//	public static interface MemberContainer extends HasTypes {
//
//		@SuppressWarnings("null")
//		default List<? extends Member> declaredMembers() {
//			return Collections.EMPTY_LIST;
//		}
//
//		@Override
//		public default List<? extends HasTypes> parentHasTypes() {
//			return parentMemberContainers();
//		}
//
//		public List<? extends MemberContainer> parentMemberContainers();
//
//		public default @Nullable Member getDeclaredMember(final String name) {
//			for (final Member m : declaredMembers()) {
//				if (m instanceof NamedElement && name.equals(((NamedElement) m).name()))
//					return m;
//			}
//			return null;
//		}
//
//		// TODO where to check name conflicts?
//		public default @Nullable Member getMember(final String name) {
//			Member m = getDeclaredMember(name);
//			if (m != null)
//				return m;
//			for (final MemberContainer hm : parentMemberContainers()) {
//				assert hm != this : this;
//				m = hm.getMember(name);
//				if (m != null) {
//					if (!m.isInherited())
//						return null;
//					return m;
//				}
//			}
//			return null;
//		}
//
//		// overridden to take optional inheritance into account
//		@Override
//		default @Nullable TypeDeclaration getType(final String name) {
//			final Member m = getMember(name);
//			return m instanceof TypeDeclaration ? (TypeDeclaration) m : null;
//		}
//
//		@Override
//		default List<? extends TypeDeclaration> declaredTypes() {
//			final ArrayList<TypeDeclaration> r = new ArrayList<>();
//			if (this instanceof TypeDeclaration)
//				r.add((TypeDeclaration) this);
//			for (final Member m : declaredMembers()) {
//				if (m instanceof TypeDeclaration)
//					r.add((TypeDeclaration) m);
//			}
//			return r;
//		}
//
//		public default @Nullable FormalAttribute getAttribute(final String name) {
//			final Member m = getMember(name);
//			return m instanceof FormalAttribute ? (FormalAttribute) m : null;
//		}
//
//	}

//	public static interface FormalModifierType extends TypeDeclaration {}

}
