package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.uses.InterpretedAttributeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;

/**
 * The native description of a Brokkr class.
 */
public class InterpretedNormalObject implements InterpretedObject {
	
	private final InterpretedSimpleClassUse type;
	
	private final Map<InterpretedAttributeDefinition, @Nullable InterpretedObject> attributeValues = new HashMap<>();
	
	public InterpretedNormalObject(final InterpretedSimpleClassUse type) {
		this.type = type;
		assert type.getBase() instanceof InterpretedNativeBrokkrClass;
		for (final InterpretedMemberRedefinition m : type.getBase().members()) {
			if (m instanceof InterpretedAttributeRedefinition && ((InterpretedAttributeRedefinition) m).isVariable())
				attributeValues.put(((InterpretedAttributeRedefinition) m).definition(), null); // TODO set default (initial) values
		}
	}
	
	@Override
	public InterpretedSimpleClassUse nativeClass() {
		return type;
	}
	
	public Map<InterpretedAttributeDefinition, @Nullable InterpretedObject> attributeValues() {
		return attributeValues;
	}
	
	public void setAttributeValue(final InterpretedAttributeDefinition definition, final InterpretedObject value) {
		if (!attributeValues.containsKey(definition))
			throw new InterpreterException("Tried to set invalid attribute " + definition + " on object of type " + nativeClass());
		attributeValues.put(definition, value);
	}
	
}
