package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTNormalResult;

public abstract class AbstractIRBrokkrResult extends AbstractIRBrokkrVariable implements IRResultRedefinition {
	
	private final IRAttributeRedefinition attribute;
	
	public AbstractIRBrokkrResult(final ASTNormalResult result, final IRAttributeRedefinition attribute) {
		super(result);
		this.attribute = attribute;
	}
	
	@Override
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
}
