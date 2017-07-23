package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;

public class IRBrokkrGenericTypeDefinition extends AbstractIRBrokkrGenericType implements IRGenericTypeDefinition {
	
	public IRBrokkrGenericTypeDefinition(final ASTGenericTypeDeclaration declaration) {
		super(declaration);
	}
	
}
