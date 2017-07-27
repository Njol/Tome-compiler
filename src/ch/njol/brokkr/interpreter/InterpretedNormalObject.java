package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrClass;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

/**
 * The native description of a Brokkr class.
 */
public class InterpretedNormalObject implements InterpretedObject {
	
	private final IRSimpleClassUse type;
	
	private final Map<IRAttributeDefinition, InterpretedObject> attributeValues = new HashMap<>();
	
	public InterpretedNormalObject(final IRSimpleClassUse type) {
		this.type = type;
		assert type.getBase() instanceof IRBrokkrClass;
		for (final IRMemberRedefinition m : type.getBase().members()) {
			if (m instanceof IRAttributeRedefinition && ((IRAttributeRedefinition) m).isVariable())
				attributeValues.put(((IRAttributeRedefinition) m).definition(), new InterpretedNullConstant()); // TODO set default (initial) values
		}
	}
	
	@Override
	public IRSimpleClassUse nativeClass() {
		return type;
	}
	
	public Map<IRAttributeDefinition, InterpretedObject> attributeValues() {
		return attributeValues;
	}
	
	public void setAttributeValue(final IRAttributeDefinition definition, final InterpretedObject value) {
		if (!attributeValues.containsKey(definition))
			throw new InterpreterException("Tried to set invalid attribute " + definition + " on object of type " + nativeClass());
		attributeValues.put(definition, value);
	}
	
	public InterpretedObject getAttributeValue(final IRAttributeDefinition definition) {
		final InterpretedObject value = attributeValues.get(definition);
		if (value == null)
			throw new InterpreterException("Tried to get invalid attribute " + definition + " from object of type " + nativeClass());
		return value;
	}
	
}
