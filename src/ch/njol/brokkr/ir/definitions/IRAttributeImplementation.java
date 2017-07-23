package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;

/**
 * An actually implemented attribute. May also redefine the signature of the attribute.
 */
public interface IRAttributeImplementation extends IRAttributeRedefinition {
	
	@Nullable
	InterpretedObject interpretImplementation(InterpretedObject thisObject, Map<IRParameterDefinition, InterpretedObject> arguments, boolean allResults);
	
	/*
	@Override
	default IRType type() {
		return new IRGenericType(interpreter.getType("lang", isModifying() ? "Procedure" :  "Function"),
				new IRGenericType(interpreter.getType("lang", "Tuple"), parameters().stream().map(p -> p.type()).toArray(i -> new IRType[i])),
				new IRGenericType(interpreter.getType("lang", "Tuple"), results().stream().map(p -> p.type()).toArray(i -> new IRType[i])));
	}
	
	@Override
	default Map<IRAttributeImplementation, IRObject> attributeValues() {
		return Collections.EMPTY_MAP;
	}
	*/
}
