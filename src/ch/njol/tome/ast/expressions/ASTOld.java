package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.members.ASTAttributeDeclaration;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IROld;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * The keyword/"function" 'old' which evaluates an expression as if it were evaluated at the beginning of its parent function.
 */
public class ASTOld extends AbstractASTElement implements ASTExpression {
	public @Nullable ASTExpression expression;
	
	public static ASTOld parse(final Parser parent) {
		return parent.one(p -> {
			final ASTOld ast = new ASTOld();
			p.one("old");
			p.oneGroup('(', () -> {
				ast.expression = ASTExpressions.parse(p);
			}, ')');
			return ast;
		});
	}
	
	@Override
	public String toString() {
		return "old(" + expression + ")";
	}
	
	@Override
	public IRTypeUse getIRType() {
		return expression != null ? expression.getIRType() : new IRUnknownTypeUse(getIRContext());
	}
	
	@Override
	public IRExpression getIR() {
		// TODO make sure to register this to the attribute so that it can be calculated when the attribute is called (and the IROld just returns that value later)
		final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
		final ASTExpression expression = this.expression;
		if (attribute == null)
			return new IRUnknownExpression("Internal compiler error", this);
		if (expression == null)
			return new IRUnknownExpression("Syntax error. Proper syntax: [old(some expression)]", this);
		return new IROld(attribute.getIR(), expression.getIR());
	}
}
