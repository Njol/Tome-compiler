package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;

public class IRBrokkrAttributeRedefinition extends AbstractIRBrokkrAttribute {
	
	public IRBrokkrAttributeRedefinition(final ASTAttributeDeclaration declaration, final IRAttributeRedefinition overridden) {
		super(declaration, overridden);
	}
	
}
