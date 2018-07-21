package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;

public class IRBrokkrAttributeDefinition extends AbstractIRBrokkrAttribute implements IRAttributeDefinition {
	
	public IRBrokkrAttributeDefinition(final ASTAttributeDeclaration declaration) {
		super(declaration);
	}
	
}
