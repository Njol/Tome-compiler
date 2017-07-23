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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attribute.hashCode();
		result = prime * result + name.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractIRBrokkrParameter other = (AbstractIRBrokkrParameter) obj;
		// TODO make sure params and error params are different
		if (!attribute.equals(other.attribute))
			return false;
		if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
