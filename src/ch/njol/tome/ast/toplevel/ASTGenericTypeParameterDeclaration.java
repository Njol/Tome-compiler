package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrGenericTypeParameter;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRGenericTypeParameter;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.parser.Parser;

public class ASTGenericTypeParameterDeclaration extends AbstractASTElementWithIR<IRGenericTypeParameter> implements ASTGenericParameterDeclaration<IRGenericTypeParameter> {
	
	public static final class ASTGenericTypeParameterDeclarationLink extends ASTLink<IRGenericTypeDefinition> {
		
		@Override
		protected @Nullable IRGenericTypeDefinition tryLink(String name) {
			ASTTypeDeclaration<?> type = getParentOfType(ASTTypeDeclaration.class);
			if (type == null)
				return null;
			IRMemberRedefinition member = type.getIR().getMemberByName(name);
			if (member instanceof IRGenericTypeDefinition)
				return (IRGenericTypeDefinition) member;
			return null;
		}
		
		public static ASTGenericTypeParameterDeclarationLink finishParsing(Parser p, UppercaseWordToken name) {
			return finishParsing(new ASTGenericTypeParameterDeclarationLink(), p, name);
		}
		
	}
	
	public final ASTGenericTypeParameterDeclarationLink link;
	
	private ASTGenericTypeParameterDeclaration(final ASTGenericTypeParameterDeclarationLink link) {
		this.link = link;
	}
	
	@Override
	public String toString() {
		return "" + link;
	}
	
	public static ASTGenericTypeParameterDeclaration finishParsing(final Parser p, final UppercaseWordToken name) {
		Parser p2 = p.startNewParent();
		ASTGenericTypeParameterDeclarationLink link = ASTGenericTypeParameterDeclarationLink.finishParsing(p, name);
		final ASTGenericTypeParameterDeclaration ast = new ASTGenericTypeParameterDeclaration(link);
		return p2.done(ast);
	}
	
	@Override
	protected IRGenericTypeParameter calculateIR() {
		return new IRBrokkrGenericTypeParameter(this);
	}
	
	@SuppressWarnings("null")
	@Override
	public WordToken nameToken() {
		return (WordToken) link.getNameToken();
	}
	
//	public Variance variance() {
//		return Variance.INVARIANT; // FIXME calculate variance from positions in attributes (arguments and results)
//		// TODO what about uses as generic arguments in other types? (e.g. if List<T> has addAll(Collection<T>))
//	}
	
}
