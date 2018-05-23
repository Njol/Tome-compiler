package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTInterfaces.ASTVariable;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public abstract class AbstractIRBrokkrVariable extends AbstractIRElement implements IRVariableRedefinition {
	
	protected final ASTVariable ast;
	protected final String name;
	protected final IRTypeUse type;
	
	public AbstractIRBrokkrVariable(final ASTVariable ast) {
		this.ast = registerDependency(ast);
		final String name = ast.name();
		this.name = name == null ? "result" : name; // TODO what if it's not a result?
		type = ast.getIRType();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public IRTypeUse type() {
		return type;
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	@Override
	public String hoverInfo() {
		return "Variable " + name() + ", type: " + type();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
