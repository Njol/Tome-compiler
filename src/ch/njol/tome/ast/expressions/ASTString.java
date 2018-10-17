package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.StringToken;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRString;

/**
 * A string literal.
 */
public class ASTString extends AbstractASTElement implements ASTExpression {
	public final String value;
	
	public ASTString(final StringToken value) {
		this.value = value.value;
	}
	
	@Override
	public String toString() {
		assert value != null;
		return "'" + value.replaceAll("'", "\\'") + "'";
	}
	
	@Override
	public IRExpression getIR() {
		return new IRString(this);
	}
}
