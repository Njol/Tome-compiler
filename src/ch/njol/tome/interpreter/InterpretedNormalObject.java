package ch.njol.tome.interpreter;

import java.util.HashMap;
import java.util.Map;

import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.uses.IRSimpleClassUse;

/**
 * The native description of a Brokkr class.
 */
public class InterpretedNormalObject implements InterpretedObject {
	
	private final IRBrokkrClassDefinition clazz;
	
	private final Map<IRAttributeDefinition, InterpretedObject> attributeValues = new HashMap<>();
	
	public InterpretedNormalObject(final IRBrokkrClassDefinition clazz) throws InterpreterException {
		this.clazz = clazz;
		for (final IRMemberRedefinition m : clazz.members()) {
			if (m instanceof IRAttributeRedefinition && ((IRAttributeRedefinition) m).isVariable()) {
				final IRAttributeRedefinition attr = (IRAttributeRedefinition) m;
				final IRResultRedefinition singleResult = attr.getSingleResult();
				if (singleResult != null) {
					final IRExpression defaultValue = singleResult.defaultValue();
					if (defaultValue != null)
						attributeValues.put(attr.definition(), defaultValue.interpret(new InterpreterContext(clazz.getIRContext(), this)));
				}
				// TODO what about variable methods?
			}
		}
	}
	
	@Override
	public IRSimpleClassUse nativeClass() {
		return new IRSimpleClassUse(clazz);
	}
	
	public Map<IRAttributeDefinition, InterpretedObject> attributeValues() {
		return attributeValues;
	}
	
	public void setAttributeValue(final IRAttributeDefinition definition, final InterpretedObject value) throws InterpreterException {
		if (!attributeValues.containsKey(definition))
			throw new InterpreterException("Tried to set invalid attribute " + definition + " on object of type " + nativeClass());
		attributeValues.put(definition, value);
	}
	
	public InterpretedObject getAttributeValue(final IRAttributeDefinition definition) throws InterpreterException {
		final InterpretedObject value = attributeValues.get(definition);
		if (value == null)
			throw new InterpreterException("Tried to get invalid attribute " + definition + " from object of type " + nativeClass());
		return value;
	}
	
}
