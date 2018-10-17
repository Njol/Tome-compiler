package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;

public class ASTModuleIdentifier extends AbstractASTElement {
	public ModuleIdentifier identifier = new ModuleIdentifier();
	
	public ASTModuleIdentifier() {}
	
	@Override
	public String toString() {
		return "" + identifier;
	}
	
	public static @Nullable ASTModuleIdentifier tryParse(final Parser parent) {
		final Parser p = parent.start();
		final ASTModuleIdentifier ast = new ASTModuleIdentifier();
		final String firstPart = p.oneVariableIdentifier();
		if (firstPart == null) {
			p.expectedFatal("a module identifier");
			p.cancel();
			return null;
		}
		ast.identifier.parts.add(firstPart);
		while (true) {
			if (!p.peekNext('.'))
				break;
			final Token t = p.peekNext(1, true);
			if (t instanceof LowercaseWordToken) {
				p.next(); // skip '.'
				p.next(); // skip package name
				ast.identifier.parts.add(((LowercaseWordToken) t).word);
			} else {
				break;
			}
		}
		return p.done(ast);
	}
	
	@Override
	public @Nullable SourceCodeLinkable getLinked(Token t) {
		// TODO make link over the whole element, not only one token
		if (parent instanceof ASTModule)
			return null;
		ASTModule ownModule = t.getParentOfType(ASTModule.class);
		if (ownModule == null) {
			final ASTSourceFile bf = t.getParentOfType(ASTSourceFile.class);
			if (bf != null)
				ownModule = bf.module;
		}
		return ownModule == null ? null
				: ownModule.modules.get(identifier);
	}
	
}
