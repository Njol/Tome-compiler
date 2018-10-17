package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.parser.Parser;

public class ASTModuleDeclaration extends AbstractASTElement {
	public @Nullable ASTModuleIdentifier module;
	
	@Override
	public String toString() {
		return "" + module;
	}
	
	public static ASTModuleDeclaration parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTModuleDeclaration ast = new ASTModuleDeclaration();
		p.one("module");
		p.until(() -> {
			ast.module = ASTModuleIdentifier.tryParse(p);
		}, ';', false);
		return p.done(ast);
	}
}
