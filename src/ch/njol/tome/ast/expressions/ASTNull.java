package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.expressions.IRNull;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'null', representing 'no value' for 'optional' variables.
 */
public class ASTNull extends AbstractASTElementWithIR<IRNull> implements ASTExpression<IRNull> {
	
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
	protected IRNull calculateIR() {
		return new IRNull(this);
	}
	
}
