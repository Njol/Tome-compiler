package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRUnknownGenericArgument;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.parser.Parser;

public class ASTGenericArgumentExpressionValue extends AbstractASTElementWithIR<IRGenericArgument> implements ASTGenericArgumentValue {
	
	private @Nullable ASTExpression<?> expression;
	
	@Override
	public String toString() {
		return "" + expression;
	}
	
	public static ASTGenericArgumentExpressionValue parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgumentExpressionValue ast = new ASTGenericArgumentExpressionValue();
			ast.expression = ASTExpressions.parse(p);
			return ast;
		});
	}
	
	@Override
	protected IRGenericArgument calculateIR() {
		return expression != null ? new IRValueGenericArgument(expression.getIR()) : new IRUnknownGenericArgument("missing value", this);
	}
	
}
