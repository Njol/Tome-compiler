package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRNull;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'null', representing 'no value' for 'optional' variables.
 */
public class ASTNull extends AbstractASTElement implements ASTExpression {
	@Override
	public String toString() {
		return "null";
	}
	
	public static ASTNull parse(final Parser parent) {
		return parent.one(p -> {
			final ASTNull ast = new ASTNull();
			p.one("null");
			return ast;
		});
	}
	
	@Override
	public IRExpression getIR() {
		return new IRNull(this);
	}
}
