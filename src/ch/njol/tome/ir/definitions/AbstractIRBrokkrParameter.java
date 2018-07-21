package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ir.IRContext;

public abstract class AbstractIRBrokkrParameter extends AbstractIRBrokkrVariable implements IRParameterRedefinition {
	
	protected final ASTParameter ast;
	protected final IRAttributeRedefinition attribute;
	
	public AbstractIRBrokkrParameter(final ASTParameter ast, final IRAttributeRedefinition attribute) {
		super(ast);
		this.ast = ast; // already registered by AbstractIRBrokkrVariable
		this.attribute = registerDependency(attribute);
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
