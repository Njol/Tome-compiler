package ch.njol.tome.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.expressions.ASTArgument;
import ch.njol.tome.ast.expressions.ASTAttributeAssignment;
import ch.njol.tome.ast.expressions.ASTDirectAttributeAccess;
import ch.njol.tome.ast.members.ASTMemberModifiers;
import ch.njol.tome.ast.toplevel.ASTGenericParameterDeclaration;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.util.Cache;

public class ASTInterfaces {
	
	public static interface NamedASTElement extends ASTElement {
		
		@Nullable
		WordOrSymbols nameToken();
		
		public default @Nullable String name() {
			final WordOrSymbols t = nameToken();
			return t == null ? null : t.wordOrSymbols();
		}
		
		@Override
		public default int linkStart() {
			final WordOrSymbols t = nameToken();
			return t == null ? absoluteRegionStart() : t.absoluteRegionStart();
		}
		
		@Override
		public default int linkLength() {
			final WordOrSymbols t = nameToken();
			return t == null ? regionLength() : t.regionLength();
		}
	}
	
	public static interface TypedASTElement extends ASTElement {
		/**
		 * @return The type of this element, or {@link IRUnknownTypeUse} if there is a compilation error.
		 */
		public IRTypeUse getIRType();
	}
	
	public static interface ASTElementWithIR<IR extends @Nullable IRElement> extends ASTElement {
		public Cache<? extends IR> irChache();
		
		public default IR getIR() {
			return irChache().get();
		}
	}
	
	/**
	 * A method call with arguments. Used by {@link ASTArgument} to find its linked parameter.
	 */
	public static interface ASTMethodCall extends ASTElement {
		public @Nullable IRAttributeRedefinition attribute();
	}
	
	/**
	 * Used by the links in {@link ASTDirectAttributeAccess} and {@link ASTAttributeAssignment} .
	 */
	public static interface ASTTargettedExpression extends ASTElement {
		public @Nullable IRTypeUse targetType();
	}
	
	public static interface ASTVariableOrAttribute extends NamedASTElement, TypedASTElement {}
	
	public static interface ASTVariable extends ASTVariableOrAttribute {
		
	}
	
	public static interface ASTLocalVariable extends ASTVariable, ASTElementWithIR<IRVariableRedefinition> {
		
	}
	
	public static interface ASTParameter extends ASTVariable, ASTElementWithIR<IRParameterRedefinition> {
		
//		public @Nullable FormalParameter overridden();
	
	}
	
	public static interface ASTResult extends TypedASTElement, NamedASTElement, ASTElementWithIR<IRResultRedefinition> {
		
	}
	
	public static interface ASTAttribute extends ASTVariableOrAttribute, ASTElementWithVariables, ASTMember, ASTElementWithIR<IRAttributeRedefinition> {
		public ASTMemberModifiers modifiers();
		
		public List<? extends ASTError<?>> declaredErrors();
		
		public default @Nullable IRError getError(final String name) {
			for (final ASTError<?> e : declaredErrors()) {
				if (name.equals(e.name()))
					return e.getIR();
			}
			final IRMemberRedefinition parent = modifiers().overridden();
			return parent != null && parent instanceof IRAttributeRedefinition ? ((IRAttributeRedefinition) parent).getErrorByName(name) : null;
		}
		
		public List<? extends ASTResult> declaredResults();
		
		public default @Nullable IRResultRedefinition getResult(final String name) {
			for (final ASTResult r : declaredResults()) {
				if (name.equals(r.name()))
					return r.getIR();
			}
			final IRMemberRedefinition parent = modifiers().overridden();
			if (parent != null && parent instanceof IRAttributeRedefinition) {
				final IRResultRedefinition r = ((IRAttributeRedefinition) parent).getResultByName(name);
				if (r != null)
					return r;
			}
			if ("result".equals(name) && declaredResults().size() > 0) {
				final ASTResult first = declaredResults().get(0);
				if (first.name() == null)
					return first.getIR();
			}
			return null;
		}
		
		public default List<IRResultRedefinition> allResults() {
			return getIR().results();
		}
		
		/**
		 * @return The type of the main result of this method, or the type of this field.
		 */
		@Override
		public default IRTypeUse getIRType() {
			final IRResultRedefinition result = getResult("result");
			return result == null ? IRTypeTuple.emptyTuple(getIRContext()) : result.type();
		}
		
		/**
		 * @return A tuple of all result types of this method (or a tuple with a single type if a field)
		 */
		public default IRTypeTuple allTypes() {
			final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(getIRContext());
			for (final IRResultRedefinition r : allResults())
				builder.addEntry(r.name(), r.type());
			return builder.build();
		}
		
		@Override
		default List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.singletonList(getIR());
		}
		
	}
	
	public static interface ASTElementWithVariables extends ASTElement {
		
		public List<? extends IRVariableRedefinition> allVariables();
		
		public default @Nullable IRVariableRedefinition getVariableByName(final String name) {
			for (final IRVariableRedefinition v : allVariables()) {
				if (name.equals(v.name()))
					return v;
			}
			return null;
		}
		
	}
	
	public static interface ASTError<IR extends IRError> extends NamedASTElement, ASTElementWithIR<IR> {
		
	}
	
	public static interface ASTExpression<IR extends IRExpression> extends TypedASTElement, ASTElementWithIR<IR> {
		
		@Override
		default IRTypeUse getIRType() {
			return getIR().type();
		}
		
	}
	
	/**
	 * A type declaration, with members and possible supertypes (currently either an interface, class, generic type, enum, or enum constant declaration).
	 */
	public static interface ASTTypeDeclaration<IR extends IRTypeDefinition> extends NamedASTElement, ASTElementWithIR<IR> {
		
		List<? extends ASTMember> declaredMembers();
		
		List<? extends ASTGenericParameterDeclaration<?>> genericParameters();
		
		// TODO remove?
		@Nullable
		IRTypeUse parentTypes();
		
		// without this, getParentOfType(ASTTypeDeclaration.class).getIR() does not properly result in an IRTypeDefinition...
		@Override
		IR getIR();
		
	}
	
	public static interface ASTMember extends ASTElement {
		
		/**
		 * @return Whether this member is also visible from subtypes. Usually true.
		 */
		boolean isInherited();
		
		/**
		 * @return The intermediate representation(s) of this member.
		 */
		List<? extends IRMemberRedefinition> getIRMembers();
		
	}
	
//	public static interface ASTGenericParameter {
//		enum Variance {
//			COVARIANT, CONTRAVARIANT, INVARIANT;
//		}
//
//		public Variance variance();
//
//		public @Nullable IRAttributeRedefinition declaration();
//	}
	
	/**
	 * A type use, e.g. in 'x(String): Int' or 'var x = String'.
	 * It is also automatically a TypedElement whose type is Type&lt;<i>this</i>&gt;
	 */
	public static interface ASTTypeUse<IR extends IRTypeUse> extends TypedASTElement, ASTElementWithIR<IR> {
		
		@Override
		default IRTypeUse getIRType() {
			return getIR().type();
		}
		
	}
	
	public static interface ASTTypeExpression<IR extends IRTypeUse> extends ASTTypeUse<IR>, ASTExpression<IR> {
		
		@Override
		default IRTypeUse getIRType() {
			return ASTExpression.super.getIRType();
		}
		
	}
	
}
