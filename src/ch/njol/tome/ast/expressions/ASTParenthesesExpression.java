package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.parser.Parser;

public class ASTParenthesesExpression extends AbstractASTElement implements ASTExpression {
	
	private @Nullable ASTExpression expression;
	
	@Override
	public IRExpression getIR() {
		return expression != null ? expression.getIR() : new IRUnknownExpression("Missing expression after opening bracket '('", this);
	}
	
	@Override
	public String toString() {
		return "(" + expression + ")";
	}
	
	public static ASTParenthesesExpression parse(final Parser parent) {
		return parent.one(p -> {
			final ASTParenthesesExpression ast = new ASTParenthesesExpression();
			p.one('(');
			p.until(() -> {
				ast.expression = ASTExpressions.parse(p);
			}, ')', false);
			return ast;
		});
	}
	
}
