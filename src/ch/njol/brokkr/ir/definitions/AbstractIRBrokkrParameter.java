package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTParameter;
import ch.njol.brokkr.ir.IRContext;

public abstract class AbstractIRBrokkrParameter extends AbstractIRBrokkrVariable implements IRParameterRedefinition {
	
	protected final ASTParameter ast;
	protected final IRAttributeRedefinition attribute;
	
	public AbstractIRBrokkrParameter(final ASTParameter ast, final IRAttributeRedefinition attribute) {
		super(ast);
		this.ast = ast;
		this.attribute = attribute;
	}
	
	@Override
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
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
