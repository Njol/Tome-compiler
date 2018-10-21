package ch.njol.tome.ast.expressions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTModifierTypeUse.ASTModifierTypeUseModifier;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTOperatorExpressionPart extends AbstractASTElement implements ASTExpression {
	
	private final ASTLink<IRAttributeRedefinition> prefixOperator;
	public @Nullable ASTExpression expression;
	
	public ASTOperatorExpressionPart(final ASTLink<IRAttributeRedefinition> prefixOperator) {
		this.prefixOperator = prefixOperator;
	}
	
	@Override
	public String toString() {
		return "" + prefixOperator + expression;
	}
	
	public static @Nullable ASTExpression parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTModifierTypeUseModifier mod = ASTModifierTypeUseModifier.tryParse(p);
		if (mod != null) {
			final Parser p2 = p.startNewParent();
			final ASTTypeExpression e = ASTModifierTypeUse.finishParsing(p, mod);
			p2.doneAsChildren();
			return e;
		}
		
		if (!p.peekNextOneOf('!', '-')) {
			p.cancel();
			return ASTAccessExpression.parse(parent);
		}
		
		final ASTOperatorExpressionPart ast = new ASTOperatorExpressionPart(ASTOperatorLink.parse(p, false, '!', '-'));
		ASTExpression expr;
		ast.expression = expr = ASTAccessExpression.parse(p);
		if (expr == null)
			p.expectedFatal("an expression");
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIRType() {
		final IRAttributeRedefinition attributeRedefinition = prefixOperator.get();
		return attributeRedefinition == null ? new IRUnknownTypeUse(getIRContext()) : attributeRedefinition.mainResultType();
	}
	
	@Override
	public IRExpression getIR() {
		final IRAttributeRedefinition attribute = prefixOperator.get();
		if (attribute == null) {
			final WordOrSymbols op = prefixOperator.getNameToken();
			return new IRUnknownExpression("Cannot find the attribute for the prefix operator " + op, op == null ? this : op);
		}
		final ASTExpression expression = this.expression;
		if (expression == null)
			return new IRUnknownExpression("Syntax error, expected an expression", this);
		final IRExpression target = expression.getIR();
		return new IRAttributeAccess(target, attribute, Collections.EMPTY_MAP, false, false, false);
	}
	
}
