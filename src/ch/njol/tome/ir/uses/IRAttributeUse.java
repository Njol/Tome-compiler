package ch.njol.tome.ir.uses;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRResultDefinition;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;

/**
 * A used attribute, i.e. an attribute of a type use, which may or may not have a target type and argument types set (or only partially).
 */
public class IRAttributeUse extends AbstractIRElement implements IRMemberUse {
	
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
//	private @Nullable final Map<IRGenericTypeDefinition, IRTypeUse> inferredGenerics = null;
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
		return targetType != null ? targetType : attribute.declaringType().getUse();
	}
	
	public IRTypeTuple allResultTypes() {
		return attribute.allResultTypes();
	}
	
	public IRTypeUse mainResultType() {
		return attribute.mainResultType();
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
}
