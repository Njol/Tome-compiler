package ch.njol.tome.ast.expressions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;

public abstract class AbstractASTAssignment<T extends AbstractASTAssignment<T>> extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression> {
	
	public final WordOrSymbols assignmentOpToken;
	public final @Nullable ASTOperatorLink assignmentOpLink;
	public @Nullable ASTExpression<?> value;
	
	protected AbstractASTAssignment(final @Nullable ASTOperatorLink assignmentOpLink, final WordOrSymbols assignmentOpToken) {
		this.assignmentOpLink = assignmentOpLink;
		this.assignmentOpToken = assignmentOpToken;
	}
	
	@Override
	public IRTypeUse getIRType() {
		return value != null ? value.getIRType() : new IRUnknownTypeUse(getIRContext());
	}
	
//		protected abstract @Nullable InterpretedObject target(InterpreterContext context);
//
//		protected abstract @Nullable IRVariableOrAttributeRedefinition varOrAttribute();
//
//		@Override
//		public @Nullable InterpretedObject interpret(final InterpreterContext context) {
//			final ASTExpression expression = value;
//			if (expression == null)
//				return null;
//			InterpretedObject value = expression.interpret(context);
//			if (value == null)
//				return null;
//			final IRVariableOrAttributeRedefinition varOrAttribute = varOrAttribute();
//			if (varOrAttribute == null)
//				return null;
//			final IRAttributeRedefinition operator = assignmentOpLink != null ? assignmentOpLink.get() : null;
//			if (operator == null && assignmentOpLink != null)
//				return null;
//			if (varOrAttribute instanceof IRVariableRedefinition) {
//				final IRVariableDefinition variableDefinition = ((IRVariableRedefinition) varOrAttribute).definition();
//				if (operator != null)
//					value = operator.interpretDispatched(context.getLocalVariableValue(variableDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
//				context.setLocalVariableValue(variableDefinition, value);
//			} else {
//				final InterpretedObject target = target(context);
//				if (target == null)
//					return null;
//				final IRAttributeDefinition attributeDefinition = ((IRAttributeRedefinition) varOrAttribute).definition();
//				if (target instanceof InterpretedNormalObject) {
//					if (operator != null)
//						value = operator.interpretDispatched(((InterpretedNormalObject) target).getAttributeValue(attributeDefinition), Collections.singletonMap(operator.parameters().get(0).definition(), value), false);
//					((InterpretedNormalObject) target).setAttributeValue(attributeDefinition, value);
//				} else {// TODO tuples
//					throw new InterpreterException("Tried to set an attribute on a native object");
//				}
//			}
//			return value;
//		}
	
	protected abstract IRExpression makeAssignmentIR(IRExpression value);
	
	protected abstract IRExpression makeAccessIR();
	
	@Override
	protected final IRExpression calculateIR() {
		final ASTExpression<?> expression = value;
		if (expression == null)
			return new IRUnknownExpression("Missing right-hand side of assignment", this);
		final IRExpression val = expression.getIR();
		final ASTOperatorLink assignmentOpLink = this.assignmentOpLink;
		if (assignmentOpLink == null) // simple assignment (without operator)
			return makeAssignmentIR(val);
		final IRAttributeRedefinition operator = assignmentOpLink.get();
		if (operator == null)
			return new IRUnknownExpression("Operator with invalid parameter name: " + operator, this);
		final IRParameterRedefinition param = operator.getParameterByName("other");
		if (param == null) {
			final WordOrSymbols nameToken = assignmentOpLink.getNameToken();
			assert nameToken != null;
			return new IRUnknownExpression("Operator with invalid parameter name: " + operator, nameToken);
		}
		return makeAssignmentIR(new IRAttributeAccess(makeAccessIR(), operator, Collections.singletonMap(param.definition(), val), false, false, false));
	}
	
}
