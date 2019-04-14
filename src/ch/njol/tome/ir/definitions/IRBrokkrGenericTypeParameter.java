package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.toplevel.ASTGenericParameterDeclaration;
import ch.njol.tome.ast.toplevel.ASTGenericTypeParameterDeclaration;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRUnknownGenericTypeDefinition;

public class IRBrokkrGenericTypeParameter extends AbstractIRBrokkrGenericParameter implements IRGenericTypeParameter {
	
	private final ASTGenericTypeParameterDeclaration ast;
	
	public IRBrokkrGenericTypeParameter(final ASTGenericTypeParameterDeclaration ast) {
		this.ast = ast;
	}
	
	@Override
	protected ASTGenericParameterDeclaration<?> ast() {
		return ast;
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		IRGenericTypeDefinition linked = ast.link.get();
		ASTTypeDeclaration<?> declaredType = ast.getParentOfType(ASTTypeDeclaration.class);
		WordToken name = (WordToken) ast.link.getNameToken();
		assert name != null;
		return linked != null ? linked : new IRUnknownGenericTypeDefinition("", name,
				declaredType != null ? declaredType.getIR() : new IRUnknownTypeDefinition(getIRContext(), "", name));
	}
	
}
