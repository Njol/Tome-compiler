package ch.njol.brokkr.ir.uses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRResultDefinition;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;

/**
 * A used attribute, i.e. an attribute of a type use, which may or may not have a target type and argument types set (or only partially).
 */
public class IRAttributeUse implements IRMemberUse {
	
	private final IRAttributeRedefinition attribute;
	private @Nullable IRTypeUse targetType;
	// this map can be incomplete (e.g. while typing)
	private final Map<IRParameterDefinition, IRTypeUse> argumentTypes = new HashMap<>();
	
	public IRAttributeUse(final IRAttributeRedefinition attribute) {
		this.attribute = attribute;
	}
	
	public IRAttributeUse(final IRAttributeRedefinition attribute, final @Nullable IRTypeUse targetType, final Map<IRParameterDefinition, IRTypeUse> argumentTypes) {
		this.attribute = attribute;
		this.targetType = targetType;
		this.argumentTypes.putAll(argumentTypes);
	}
	
	// TODO infer types
	private @Nullable final Map<IRGenericTypeDefinition, IRTypeUse> inferredGenerics = null;
	private @Nullable final Map<IRResultDefinition, IRTypeUse> resultTypes = null;
	
	@Override
	public IRAttributeRedefinition redefinition() {
		return attribute;
	}
	
	@Override
	public IRAttributeDefinition definition() {
		return attribute.definition();
	}
	
	public IRTypeUse targetType() {
		return targetType != null ? targetType : attribute.declaringType().getUse(Collections.EMPTY_MAP);
	}
	
	public IRTypeTuple allResultTypes() {
		return attribute.allResultTypes();
	}
	
	public IRTypeUse mainResultType() {
		return attribute.mainResultType();
	}
	
}
