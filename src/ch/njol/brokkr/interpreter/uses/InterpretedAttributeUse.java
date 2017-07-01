package ch.njol.brokkr.interpreter.uses;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultDefinition;

public class InterpretedAttributeUse {
	
	private final InterpretedAttributeDefinition attribute;
	private final InterpretedTypeUse targetType;
	// this map can be incomplete (e.g. while typing)
	private final Map<InterpretedParameterDefinition, InterpretedTypeUse> argumentTypes;
	
	public InterpretedAttributeUse(final InterpretedAttributeDefinition attribute, final InterpretedTypeUse targetType, final Map<InterpretedParameterDefinition, InterpretedTypeUse> argumentTypes) {
		this.attribute = attribute;
		this.targetType = targetType;
		this.argumentTypes = argumentTypes;
	}
	
	private @Nullable Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> inferredGenerics = null;
	private @Nullable Map<InterpretedResultDefinition, InterpretedTypeUse> resultTypes = null;
	
}
