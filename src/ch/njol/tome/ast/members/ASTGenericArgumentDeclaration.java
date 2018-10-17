package ch.njol.tome.ast.members;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.parser.Parser;

// TODO is this even useful?
public class ASTGenericArgumentDeclaration extends AbstractASTElement {
	
	private @Nullable WordToken name;
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	public static ASTGenericArgumentDeclaration parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTGenericArgumentDeclaration ast = new ASTGenericArgumentDeclaration();
		ast.name = p.oneIdentifierToken();
		return p.done(ast);
	}
	
}
