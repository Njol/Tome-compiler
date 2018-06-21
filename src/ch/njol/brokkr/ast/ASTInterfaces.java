package ch.njol.brokkr.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTMembers.ASTMemberModifiers;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public class ASTInterfaces {
	
	public static interface NamedASTElement extends ASTElement {
		
		@Nullable
		WordToken nameToken();
		
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
	
	public static interface TypedASTElement extends ASTElement {
		/**
		 * @return The type of this element, or {@link IRUnknownTypeUse} if there is a compilation error.
		 */
		public IRTypeUse getIRType();
	}
	
	public static interface ASTElementWithIR extends ASTElement {
		public IRElement getIR();
	}
	
	public static interface ASTVariableOrAttribute extends NamedASTElement, TypedASTElement {}
	
	public static interface ASTVariable extends ASTVariableOrAttribute {
		
	}
	
	public static interface ASTLocalVariable extends ASTVariable, ASTElementWithIR {
		
		@Override
		public IRVariableRedefinition getIR();
		
	}
	
	public static interface ASTParameter extends ASTVariable, ASTElementWithIR {
		
		@Override
		IRParameterRedefinition getIR();
		
//		public @Nullable FormalParameter overridden();
	
	}
	
	public static interface ASTResult extends TypedASTElement, NamedASTElement, ASTElementWithIR {
		
		@Override
		IRResultRedefinition getIR();
		
	}
	
	public static interface ASTAttribute extends ASTVariableOrAttribute, ASTElementWithVariables, ASTMember, ASTElementWithIR {
		public ASTMemberModifiers modifiers();
		
		public List<? extends ASTError> declaredErrors();
		
		public default @Nullable IRError getError(final String name) {
			for (final ASTError e : declaredErrors()) {
				if (name.equals(e.name()))
					return e.getIRError();
			}
			final IRMemberRedefinition parent = modifiers().overridden.get();
			return parent != null && parent instanceof IRAttributeRedefinition ? ((IRAttributeRedefinition) parent).getErrorByName(name) : null;
		}
		
		public List<? extends ASTResult> declaredResults();
		
		public default @Nullable IRResultRedefinition getResult(final String name) {
			for (final ASTResult r : declaredResults()) {
				if (name.equals(r.name()))
					return r.getIR();
			}
			final IRMemberRedefinition parent = modifiers().overridden.get();
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
		
		@Override
		public IRAttributeRedefinition getIR();
		
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
	
	public static interface ASTError extends NamedASTElement {
		
		IRError getIRError();
		
	}
	
	public static interface ASTExpression extends TypedASTElement, ASTElementWithIR {
		
		@Override
		IRExpression getIR();
		
		@Override
		default IRTypeUse getIRType() {
			return getIR().type();
		}
		
	}
	
	/**
	 * A type declaration, with members and possible supertypes (currently either an interface, class, generic type, enum, or enum constant declaration).
	 */
	public static interface ASTTypeDeclaration extends NamedASTElement, ASTElementWithIR {
		
		List<? extends ASTMember> declaredMembers();
		
		List<? extends ASTGenericParameter> genericParameters();
		
		// TODO remove?
		@Nullable
		IRTypeUse parentTypes();
		
		@Override
		IRTypeDefinition getIR();
		
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
	
	public static interface ASTGenericParameter {
		enum Variance {
			COVARIANT, CONTRAVARIANT, INVARIANT;
		}
		
		public Variance variance();
		
		public @Nullable IRGenericTypeRedefinition declaration();
	}
	
	/**
	 * A type use, e.g. in 'x(String): Int' or 'var x = String'.
	 * It is also automatically a TypedElement whose type is Type&lt;<i>this</i>&gt;
	 */
	public static interface ASTTypeUse extends TypedASTElement, ASTElementWithIR {
		
		@Override
		IRTypeUse getIR();
		
		@Override
		default IRTypeUse getIRType() {
			return getIR().type();
		}
		
	}
	
	public static interface ASTTypeExpression extends ASTTypeUse, ASTExpression {
		
		@Override
		default IRTypeUse getIRType() {
			return ASTExpression.super.getIRType();
		}
		
	}
	
}
