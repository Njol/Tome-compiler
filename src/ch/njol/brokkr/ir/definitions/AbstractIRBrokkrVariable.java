package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTVariable;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public abstract class AbstractIRBrokkrVariable implements IRVariableRedefinition {
	
	protected final ASTVariable var;
	protected final String name;
	protected final IRTypeUse type;
	
	public AbstractIRBrokkrVariable(final ASTVariable var) {
		this.var = var;
		final String name = var.name();
		this.name = name == null ? "result" : name; // TODO what if it's not a result?
		type = var.getIRType();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public IRTypeUse type() {
		return type;
	}
	
}
