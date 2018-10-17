package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.parser.Parser;

/**
 * A declaration of a generic parameter of a type, i.e. a generic type that can be defined by position without a name after a type, e.g. 'List&lt;T>'.
 */
public class ASTGenericParameterDeclaration extends AbstractASTElement implements ASTGenericParameter {
	
	private @Nullable ASTGenericParameterDeclarationLink definition;
	
	private static class ASTGenericParameterDeclarationLink extends ASTLink<IRAttributeRedefinition> {
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(String name) {
			final ASTTypeDeclaration type = getParentOfType(ASTTypeDeclaration.class);
			if (type == null)
				return null;
			return type.getIR().getAttributeByName(name);
		}
		
		private static ASTGenericParameterDeclarationLink parse(Parser parent) {
			return parseAsTypeIdentifier(new ASTGenericParameterDeclarationLink(), parent);
		}
	}
	
	@Override
	public String toString() {
		return "" + definition;
	}
	
	public static ASTGenericParameterDeclaration parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTGenericParameterDeclaration ast = new ASTGenericParameterDeclaration();
		ast.definition = ASTGenericParameterDeclarationLink.parse(p);
		return p.done(ast);
	}
	
	@Override
	public Variance variance() {
		return Variance.INVARIANT; // FIXME calculate variance from positions in attributes (arguments and results)
		// TODO what about uses as generic arguments in other types? (e.g. if List<T> has addAll(Collection<T>))
	}
	
	@Override
	public @Nullable IRAttributeRedefinition declaration() {
		return definition != null ? definition.get() : null;
	}
	
}
