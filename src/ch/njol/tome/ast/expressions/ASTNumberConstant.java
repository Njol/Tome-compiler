package ch.njol.tome.ast.expressions;

import java.math.BigDecimal;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.NumberToken;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRNumberConstant;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTNumberConstant extends AbstractASTElement implements ASTExpression {
	public final BigDecimal value;
	
	private ASTNumberConstant(final NumberToken token) {
		value = token.value;
	}
	
	public static ASTNumberConstant parse(final Parser parent) {
		return parent.one(p -> {
			@SuppressWarnings("null")
			final ASTNumberConstant ast = new ASTNumberConstant((NumberToken) p.next());
			return ast;
		});
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		try {
			final long l = value.longValueExact();
			return l + " (0x" + Long.toHexString(l).toUpperCase(Locale.ENGLISH) + ")";
		} catch (final NumberFormatException e) {
			return "" + value;
		}
	}
	
	@Override
	public IRTypeUse getIRType() {
		return IRNumberConstant.type(getIRContext(), value);
	}
	
	@Override
	public IRExpression getIR() {
		return new IRNumberConstant(this);
	}
}
