package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.toplevel.ASTGenericAttributeParameterDeclaration;
import ch.njol.tome.ast.toplevel.ASTGenericParameterDeclaration;
import ch.njol.tome.ir.IRUnknownAttributeDefinition;

public class IRBrokkrGenericAttributeParameter extends AbstractIRBrokkrGenericParameter implements IRGenericAttributeParameter {
	
	private final ASTGenericAttributeParameterDeclaration ast;
	
	public IRBrokkrGenericAttributeParameter(final ASTGenericAttributeParameterDeclaration ast) {
		this.ast = ast;
	}
	
	@Override
	protected ASTGenericParameterDeclaration<?> ast() {
		return ast;
	}
	
	@Override
	public IRAttributeRedefinition definition() {
		final IRAttributeRedefinition attribute = ast.attribute();
		return attribute != null ? attribute : new IRUnknownAttributeDefinition(declaringType(), name(), "Cannot find referenced attribute [" + name() + "] in [" + declaringType() + "]", ast);
	}
	
}
