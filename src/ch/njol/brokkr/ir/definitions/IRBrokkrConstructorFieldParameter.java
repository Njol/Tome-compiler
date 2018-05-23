package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;

public class IRBrokkrConstructorFieldParameter extends AbstractIRBrokkrParameter implements IRParameterDefinition {
	
	public final IRAttributeRedefinition field;
	
	public IRBrokkrConstructorFieldParameter(final ASTParameter param, final IRAttributeRedefinition field, final IRAttributeRedefinition attribute) {
		super(param, attribute);
		this.field = registerDependency(field);
	}
	
	// TODO allow a default value here? or does that make no sense? (can make default on the fields themselves, but that doesn't make this parameter optional!)
	@Override
	public @Nullable InterpretedObject defaultValue(@NonNull final InterpreterContext context) {
		return null;
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
