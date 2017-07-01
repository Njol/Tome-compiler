package ch.njol.brokkr.interpreter.uses;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

/**
 * A type object for "normal" types, i.e. types without special handling (like tuples and "and/or" types).
 */
public class InterpretedSimpleTypeUse implements InterpretedTypeUse {
	
	private final InterpretedNativeTypeDefinition type;
	private final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments = new HashMap<>();

	/**
	 * Creates a simple type object without generic type information
	 * @param type
	 */
	public InterpretedSimpleTypeUse(InterpretedNativeTypeDefinition type) {
		this.type = type;
	}
	
	public InterpretedSimpleTypeUse(InterpretedNativeTypeDefinition base, Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments) {
		type = base;
		this.genericArguments.putAll(genericArguments);
	}
	
	public InterpretedNativeTypeDefinition getBase() {
		return type;
	}

	@Override
	public boolean equalsType(InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubtypeOfOrEqual(InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;  
	}

	@Override
	public boolean isSupertypeOfOrEqual(InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}

}
