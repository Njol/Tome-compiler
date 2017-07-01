package ch.njol.brokkr.interpreter.definitions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.data.MethodModifiability;
import ch.njol.brokkr.data.Modifiability;
import ch.njol.brokkr.interpreter.InterpretedObject;

/**
 * An actually implemented attribute. May also redefine the signature of the attribute.
 */
public interface InterpretedAttributeImplementation extends InterpretedAttributeRedefinition {
	
	@Nullable InterpretedObject interpretImplementation(InterpretedObject thisObject, Map<InterpretedParameterDefinition, InterpretedObject> arguments, boolean allResults);
	
	/*
	@Override
	default InterpretedType type() {
		return new InterpretedGenericType(interpreter.getType("lang", isModifying() ? "Procedure" :  "Function"),
				new InterpretedGenericType(interpreter.getType("lang", "Tuple"), parameters().stream().map(p -> p.type()).toArray(i -> new InterpretedType[i])),
				new InterpretedGenericType(interpreter.getType("lang", "Tuple"), results().stream().map(p -> p.type()).toArray(i -> new InterpretedType[i])));
	}

	@Override
	default Map<InterpretedAttributeImplementation, InterpretedObject> attributeValues() {
		return Collections.EMPTY_MAP;
	}
	*/
}
