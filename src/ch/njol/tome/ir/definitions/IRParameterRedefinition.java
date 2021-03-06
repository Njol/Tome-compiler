package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRParameterRedefinition extends IRVariableRedefinition {
	
	@Override
	String name();
	
	@Override
	public IRTypeUse type();
	
	@Override
	IRParameterDefinition definition();
	
	@Nullable
	InterpretedObject defaultValue(InterpreterContext context) throws InterpreterException;
	
	IRAttributeRedefinition attribute();
	
	/**
	 * @param other
	 * @return Whether this and the given parameter are equal, i.e. are defined in the same attribute and have the same name.
	 */
	default boolean equalsParameter(final IRParameterRedefinition other) {
		return attribute().equalsMember(other.attribute()) && name().equals(other.name());
	}
	
	default int parameterHashCode() {
		return attribute().memberHashCode() * 31 + name().hashCode();
	}
	
}
