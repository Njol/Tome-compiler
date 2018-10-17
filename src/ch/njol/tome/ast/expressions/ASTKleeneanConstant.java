package ch.njol.tome.ast.expressions;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.common.Kleenean;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRKleeneanConstant;
import ch.njol.tome.parser.Parser;

public class ASTKleeneanConstant extends AbstractASTElement implements ASTExpression {
	public final Kleenean value;
	
	private ASTKleeneanConstant(final Kleenean value) {
		this.value = value;
	}
	
	public static @Nullable ASTKleeneanConstant tryParse(final Parser parent) {
		final Parser p = parent.start();
		final WordOrSymbols token = p.try2("true", "false", "unknown");
		if (token == null) {
			p.cancel();
			return null;
		}
		return p.done(new ASTKleeneanConstant(Kleenean.valueOf(token.wordOrSymbols().toUpperCase(Locale.ENGLISH))));
	}
	
	@Override
	public String toString() {
		assert value != null;
		return "" + value.name().toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public IRExpression getIR() {
		return new IRKleeneanConstant(this);
	}
	
}
