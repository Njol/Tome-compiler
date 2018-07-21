package ch.njol.tome.ir.nativetypes.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeImplementation;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.nativetypes.IRTypeClassDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

/**
 * The class of a native type, e.g. the type of NativeInt8.
 */
public class IRNativeTypeClassDefinition extends AbstractIRElement implements IRTypeClassDefinition {
	
	private final IRContext irContext;
	private final Class<? extends InterpretedNativeObject> interpretedType;
	private final String name;
	
	private IRNativeTypeClassDefinition(final IRContext irContext, final Class<? extends InterpretedNativeObject> interpretedType) {
		this.irContext = irContext;
		this.interpretedType = interpretedType;
		assert interpretedType.getSimpleName().startsWith("Interpreted") : interpretedType;
		name = "" + interpretedType.getSimpleName().substring("Interpreted".length());
	}
	
	public String name() {
		return name;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	public static IRNativeTypeClassDefinition get(final IRContext irContext, final Class<? extends InterpretedNativeObject> interpretedType) {
		IRNativeTypeClassDefinition cached = irContext.nativeTypeClassCache.get(interpretedType);
		if (cached != null)
			return cached;
		cached = new IRNativeTypeClassDefinition(irContext, interpretedType);
		irContext.nativeTypeClassCache.put(interpretedType, cached);
		return cached;
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
		for (final IRAttributeImplementation attr : members()) {
			if (attr.definition().equalsMember(definition))
				return attr;
		}
		return null;
	}
	
	private @Nullable List<IRAttributeImplementation> attributes = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRAttributeImplementation> members() {
		if (attributes == null) {
			attributes = new ArrayList<>();
			for (final Method m : interpretedType.getDeclaredMethods()) {
				if (m.getName().startsWith("_"))
					attributes.add(new IRNativeMethod(m, "" + m.getName().substring(1), irContext));
			}
		}
		return attributes;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return other.getClass() == this.getClass() && interpretedType == ((IRNativeTypeClassDefinition) other).interpretedType;
	}
	
	@Override
	public int typeHashCode() {
		return name.hashCode();
	}
	
	// native classes do not implement any interfaces (not even [Any]?)
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return Collections.EMPTY_SET;
	}

	@Override
	public List<IRAttributeRedefinition> positionalGenericParameters() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public int compareTo(final IRTypeDefinition other) {
		if (other instanceof IRNativeTypeClassDefinition) {
			return name.compareTo(((IRNativeTypeClassDefinition) other).name);
		}
		return IRTypeDefinition.compareTypeDefinitionClasses(this.getClass(), other.getClass());
	}
	
}
