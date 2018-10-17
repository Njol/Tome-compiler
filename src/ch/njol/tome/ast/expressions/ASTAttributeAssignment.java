package ch.njol.tome.ast.expressions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTargettedExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRAttributeAssignment;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTAttributeAssignment extends AbstractASTAssignment<ASTAttributeAssignment> implements ASTTargettedExpression {
	
	public final ASTExpression target;
	public final @Nullable ASTLink<? extends IRAttributeRedefinition> attribute;
	
	public ASTAttributeAssignment(final ASTExpression target, final @Nullable ASTLink<? extends IRAttributeRedefinition> attribute, final @Nullable ASTOperatorLink assignmentOpLink, WordOrSymbols assignmentOpToken) {
		super(assignmentOpLink, assignmentOpToken);
		this.target = target;
		this.attribute = attribute;
	}
	
	@Override
	public String toString() {
		return target + "." + attribute + " " + assignmentOpToken + " " + value;
	}
	
	public static ASTAttributeAssignment finishParsing(final Parser p, final ASTExpression target, final @Nullable ASTLink<? extends IRAttributeRedefinition> attribute,
			final @Nullable ASTOperatorLink assignmentOpLink, WordOrSymbols assignmentOpToken) {
		final ASTAttributeAssignment ast = new ASTAttributeAssignment(target, attribute, assignmentOpLink, assignmentOpToken);
		ast.value = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public @Nullable IRTypeUse targetType() {
		return target.getIRType();
	}
	
	@Override
	protected IRExpression makeAssignmentIR(final IRExpression value) {
		final IRAttributeRedefinition attr = attribute != null ? attribute.get() : null;
		if (attr == null) {
			final WordOrSymbols a = attribute != null ? attribute.getNameToken() : null;
			return new IRUnknownExpression("Cannot find an attribute named " + attribute + " in the type " + target.getIRType(), a == null ? this : a);
		}
		return new IRAttributeAssignment(target.getIR(), attr.definition(), value);
	}
	
	@Override
	protected IRExpression makeAccessIR() {
		final IRAttributeRedefinition attr = attribute != null ? attribute.get() : null;
		if (attr == null) {
			final WordOrSymbols a = attribute != null ? attribute.getNameToken() : null;
			return new IRUnknownExpression("Cannot find an attribute named " + attribute + " in the type " + target.getIRType(), a == null ? this : a);
		}
		return new IRAttributeAccess(target.getIR(), attr.definition(), Collections.EMPTY_MAP, false, false, false);
	}
	
}
