package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTNormalResult;

public class IRBrokkrResultRedefinition extends AbstractIRBrokkrResult {
	
	private final IRResultRedefinition overridden;
	
	public IRBrokkrResultRedefinition(final ASTNormalResult result, final IRResultRedefinition overridden, final IRAttributeRedefinition attribute) {
		super(result, attribute);
		this.overridden = overridden;
	}
	
	@Override
	public IRResultDefinition definition() {
		return overridden.definition();
	}
	
}
