package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.members.ASTNormalResult;

public class IRBrokkrResultDefinition extends AbstractIRBrokkrResult implements IRResultDefinition {
	
	public IRBrokkrResultDefinition(final ASTNormalResult result, final IRAttributeRedefinition attribute) {
		super(result, attribute);
	}
	
	@Override
	public IRBrokkrResultDefinition definition() {
		return this;
	}
	
}
