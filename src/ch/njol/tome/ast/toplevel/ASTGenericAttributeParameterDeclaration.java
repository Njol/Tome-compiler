package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrGenericAttributeParameter;
import ch.njol.tome.ir.definitions.IRGenericParameter;
import ch.njol.tome.parser.Parser;

public class ASTGenericAttributeParameterDeclaration extends AbstractASTElementWithIR<IRGenericParameter> implements ASTGenericParameterDeclaration<IRGenericParameter> {
	
	private final ASTGenericAttributeParameterDeclarationLink attribute;
	
	private ASTGenericAttributeParameterDeclaration(final ASTGenericAttributeParameterDeclarationLink attribute) {
		this.attribute = attribute;
	}
	
	private static class ASTGenericAttributeParameterDeclarationLink extends ASTLink<IRAttributeRedefinition> {
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(final String name) {
			final ASTTypeDeclaration<?> type = getParentOfType(ASTTypeDeclaration.class);
			if (type == null)
				return null;
			return type.getIR().getAttributeByName(name);
		}
		
		private static ASTGenericAttributeParameterDeclarationLink finishParsing(final Parser p, final LowercaseWordToken name) {
			return finishParsing(new ASTGenericAttributeParameterDeclarationLink(), p, name);
		}
	}
	
	public static ASTGenericAttributeParameterDeclaration finishParsing(final Parser p, final LowercaseWordToken name) {
		final Parser p2 = p.startNewParent();
		final ASTGenericAttributeParameterDeclarationLink link = ASTGenericAttributeParameterDeclarationLink.finishParsing(p, name);
		final ASTGenericAttributeParameterDeclaration ast = new ASTGenericAttributeParameterDeclaration(link);
		return p2.done(ast);
	}
	
	@Override
	public String toString() {
		return "" + attribute;
	}
	
	@SuppressWarnings("null")
	@Override
	public WordOrSymbols nameToken() {
		return attribute.getNameToken();
	}
	
	@Override
	protected IRGenericParameter calculateIR() {
		return new IRBrokkrGenericAttributeParameter(this);
	}
	
	public @Nullable IRAttributeRedefinition attribute() {
		return attribute.get();
	}
	
}
