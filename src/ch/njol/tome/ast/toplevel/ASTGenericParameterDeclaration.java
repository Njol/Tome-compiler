package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithIR;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRGenericParameter;
import ch.njol.tome.parser.Parser;

public interface ASTGenericParameterDeclaration<IR extends IRGenericParameter> extends ASTElementWithIR<IR>, NamedASTElement {
	
	static @Nullable ASTGenericParameterDeclaration<?> parse(final Parser parent) {
		final Parser p = parent.start();
		final WordToken name = p.oneIdentifierToken();
		if (name == null) {
			p.cancel();
			return null;
		} else if (name instanceof UppercaseWordToken) {
			return ASTGenericTypeParameterDeclaration.finishParsing(p, (UppercaseWordToken) name);
		} else {
			return ASTGenericAttributeParameterDeclaration.finishParsing(p, (LowercaseWordToken) name);
		}
	}
	
	@Override
	public default @NonNull String name() {
		return nameToken().wordOrSymbols();
	}
	
	@Override
	public @NonNull WordOrSymbols nameToken();
	
}
