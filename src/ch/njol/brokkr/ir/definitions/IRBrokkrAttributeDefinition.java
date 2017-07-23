package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTAttributeDeclaration;

public class IRBrokkrAttributeDefinition extends AbstractIRBrokkrAttribute implements IRAttributeDefinition {
	
	public IRBrokkrAttributeDefinition(final ASTAttributeDeclaration declaration) {
		super(declaration);
	}
	
}
