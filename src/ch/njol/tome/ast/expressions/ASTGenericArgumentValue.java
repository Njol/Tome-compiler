package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRUnknownGenericArgument;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.parser.Parser;

public class ASTGenericArgumentValue extends AbstractASTElement {
	
	private @Nullable ASTExpression expression;
	
	@Override
	public String toString() {
		return "" + expression;
	}
	
	public static ASTGenericArgumentValue parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgumentValue ast = new ASTGenericArgumentValue();
//		if (try_('?')) {
//			wildcard = true;
//			unordered(() -> {
//				if (try_("extends"))
//					extends_ = ActualType.parse(this, true, true);
//			}, () -> {
//				if (try_("super"))
//					super_ = ActualType.parse(this, true, true);
//			});
//		} else {
//			value = ActualType.parse(this, true, true);
//		}
			ast.expression = ASTExpressions.parse(p, false);
			return ast;
		});
	}
	
	public IRGenericArgument getIR() {
		return expression != null ? IRValueGenericArgument.fromExpression(expression) : new IRUnknownGenericArgument("missing value", this);
	}
	
}
