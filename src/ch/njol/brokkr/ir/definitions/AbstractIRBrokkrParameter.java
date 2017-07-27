package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTParameter;

public abstract class AbstractIRBrokkrParameter extends AbstractIRBrokkrVariable implements IRParameterRedefinition {
	
	protected final ASTParameter param;
	protected final IRAttributeRedefinition attribute;
	
	public AbstractIRBrokkrParameter(final ASTParameter param, final IRAttributeRedefinition attribute) {
		super(param);
		this.param = param;
		this.attribute = attribute;
	}
	
	@Override
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
	@Override
	public int hashCode() {
		return parameterHashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object other) {
		return other instanceof IRParameterRedefinition ? equalsParameter((IRParameterRedefinition) other) : false;
	}
	
}
