package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;

public class InterpretedBrokkrConstructorFieldParameter extends AbstractInterpretedBrokkrParameter implements InterpretedParameterDefinition {
	
	public final InterpretedAttributeRedefinition field;
	
	public InterpretedBrokkrConstructorFieldParameter(final FormalParameter param, final InterpretedAttributeRedefinition field, final InterpretedAttributeRedefinition attribute) {
		super(param, attribute);
		this.field = field;
	}
	
	// TODO allow a default value here? or does that make no sense? (can make default on the fields themselves, but that doesn't make this parameter optional!)
	@Override
	public @Nullable InterpretedObject defaultValue(@NonNull InterpreterContext context) {
		return null;
	}
	
}
