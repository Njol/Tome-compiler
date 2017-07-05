package ch.njol.brokkr.interpreter.definitions;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariable;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public abstract class AbstractInterpretedBrokkrVariable implements InterpretedVariableRedefinition {
	
	protected final FormalVariable var;
	protected final String name;
	protected final InterpretedTypeUse type;
	
	public AbstractInterpretedBrokkrVariable(final FormalVariable var) {
		this.var = var;
		final String name = var.name();
		this.name = name == null ? "result" : name; // TODO what if it's not a result?
		type = var.interpretedType();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public InterpretedTypeUse type() {
		return type;
	}
	
}
