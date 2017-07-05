package ch.njol.brokkr.interpreter.nativetypes.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;

/**
 * The native description of a native type, e.g. the type of NativeInt8.
 */
public class InterpretedNativeSimpleNativeClass implements InterpretedNativeBrokkrTypeDefinition, InterpretedNativeClassDefinition {
	
	private final Class<? extends InterpretedNativeObject> interpretedType;
	private final String name;
	private final List<InterpretedAttributeImplementation> attributes = new ArrayList<>();
	
	private InterpretedNativeSimpleNativeClass(final Class<? extends InterpretedNativeObject> interpretedType) {
		this.interpretedType = interpretedType;
		assert interpretedType.getSimpleName().startsWith("Interpreted");
		name = "" + interpretedType.getSimpleName().substring("Interpreted".length());
		for (final Method m : interpretedType.getDeclaredMethods()) {
			if (m.getName().startsWith("_"))
				attributes.add(new InterpretedNativeNativeMethod(m, "" + m.getName().substring(1)));
		}
	}
	
	public String name() {
		return name;
	}
	
	private static Map<Class<? extends InterpretedNativeObject>, InterpretedNativeSimpleNativeClass> cache = new HashMap<>();
	
	public static InterpretedNativeSimpleNativeClass get(final Class<? extends InterpretedNativeObject> interpretedType) {
		InterpretedNativeSimpleNativeClass cached = cache.get(interpretedType);
		if (cached != null)
			return cached;
		cached = new InterpretedNativeSimpleNativeClass(interpretedType);
		cache.put(interpretedType, cached);
		return cached;
	}
	
	@Override
	public @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
		for (final InterpretedAttributeImplementation attr : attributes) {
			if (attr.definition().equalsMember(definition))
				return attr;
		}
		return null;
	}
	
	@Override
	public List<? extends InterpretedMemberRedefinition> members() {
		return attributes;
	}
	
	@Override
	public boolean equalsType(final InterpretedNativeTypeDefinition other) {
		return other.getClass() == this.getClass() && interpretedType == ((InterpretedNativeSimpleNativeClass) other).interpretedType;
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final InterpretedNativeTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
