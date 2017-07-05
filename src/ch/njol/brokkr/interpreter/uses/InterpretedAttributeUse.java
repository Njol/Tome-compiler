package ch.njol.brokkr.interpreter.uses;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedTypeTuple;

/**
 * A used attribute, i.e. an attribute of a type use, which may or may not have a target type and argument types set (or only partially).
 */
public class InterpretedAttributeUse implements InterpretedMemberUse {
	
	private final InterpretedAttributeRedefinition attribute;
	private @Nullable InterpretedTypeUse targetType;
	// this map can be incomplete (e.g. while typing)
	private final Map<InterpretedParameterDefinition, InterpretedTypeUse> argumentTypes = new HashMap<>();
	
	public InterpretedAttributeUse(final InterpretedAttributeRedefinition attribute) {
		this.attribute = attribute;
	}
	
	public InterpretedAttributeUse(final InterpretedAttributeRedefinition attribute, final @Nullable InterpretedTypeUse targetType, final Map<InterpretedParameterDefinition, InterpretedTypeUse> argumentTypes) {
		this.attribute = attribute;
		this.targetType = targetType;
		this.argumentTypes.putAll(argumentTypes);
	}
	
	// TODO infer types
	private @Nullable final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> inferredGenerics = null;
	private @Nullable final Map<InterpretedResultDefinition, InterpretedTypeUse> resultTypes = null;
	
	@Override
	public InterpretedAttributeRedefinition redefinition() {
		return attribute;
	}
	
	@Override
	public InterpretedAttributeDefinition definition() {
		return attribute.definition();
	}
	
	public InterpretedTypeUse targetType() {
		return targetType != null ? targetType : attribute.targetType();
	}
	
	public InterpretedTypeTuple allResultTypes() {
		return attribute.allResultTypes();
	}
	
	public InterpretedTypeUse mainResultType() {
		return attribute.mainResultType();
	}
	
}
