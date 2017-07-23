package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTNormalResult;

public class IRBrokkrResultDefinition extends AbstractIRBrokkrResult implements IRResultDefinition {
	
	public IRBrokkrResultDefinition(final ASTNormalResult result, final IRAttributeRedefinition attribute) {
		super(result, attribute);
	}
	
	@Override
	public IRBrokkrResultDefinition definition() {
		return this;
	}
	
}
