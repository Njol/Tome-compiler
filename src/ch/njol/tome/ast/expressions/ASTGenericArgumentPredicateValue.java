package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRPredicateGenericArgument;
import ch.njol.tome.ir.IRUnknownGenericArgument;
import ch.njol.tome.parser.Parser;

public class ASTGenericArgumentPredicateValue extends AbstractASTElementWithIR<IRGenericArgument> implements ASTGenericArgumentValue {
	
	private @Nullable ASTExpression<?> predicate;
	
	@Override
	public String toString() {
		return "{" + predicate + "}";
	}
	
	public static ASTGenericArgumentPredicateValue parse(final Parser parent) {
		return parent.one(p -> {
			final ASTGenericArgumentPredicateValue ast = new ASTGenericArgumentPredicateValue();
			p.oneGroup('{', () -> {
				ast.predicate = ASTExpressions.parse(p);
			}, '}');
			return ast;
		});
	}
	
	@Override
	protected IRGenericArgument calculateIR() {
		return predicate != null ? new IRPredicateGenericArgument(predicate.getIR()) : new IRUnknownGenericArgument("missing value", this);
	}
	
}
