package ch.njol.brokkr.ir.nativetypes.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRClassDefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * The native description of a native type, e.g. the type of NativeInt8.
 */
public class IRSimpleNativeClass implements IRClassDefinition {
	
	private final Class<? extends InterpretedNativeObject> interpretedType;
	private final String name;
	
	private IRSimpleNativeClass(final Class<? extends InterpretedNativeObject> interpretedType) {
		this.interpretedType = interpretedType;
		assert interpretedType.getSimpleName().startsWith("Interpreted") : interpretedType;
		name = "" + interpretedType.getSimpleName().substring("Interpreted".length());
	}
	
	public String name() {
		return name;
	}
	
	private static Map<Class<? extends InterpretedNativeObject>, IRSimpleNativeClass> cache = new HashMap<>();
	
	public static IRSimpleNativeClass get(final Class<? extends InterpretedNativeObject> interpretedType) {
		IRSimpleNativeClass cached = cache.get(interpretedType);
		if (cached != null)
			return cached;
		cached = new IRSimpleNativeClass(interpretedType);
		cache.put(interpretedType, cached);
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
					attributes.add(new IRNativeNativeMethod(m, "" + m.getName().substring(1)));
			}
		}
		return attributes;
	}
	
	@Override
	public boolean equalsType(final IRTypeDefinition other) {
		return other.getClass() == this.getClass() && interpretedType == ((IRSimpleNativeClass) other).interpretedType;
	}
	
	@Override
	public int typeHashCode() {
		return name.hashCode();
	}
	
	// native classes do not implement any interfaces
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeDefinition other) {
		return equalsType(other);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeDefinition other) {
		return equalsType(other);
	}
	
}
