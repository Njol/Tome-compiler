package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;

/**
 * The native description of a Brokkr class.
 */
public class InterpretedNormalObject implements InterpretedObject {
	
	private final IRSimpleClassUse type;
	
	private final Map<IRAttributeDefinition, InterpretedObject> attributeValues = new HashMap<>();
	
	/**
	 * Creates a new object of the given type
	 * 
	 * @param type A type use of an {@link IRBrokkrClassDefinition}.
	 * @throws InterpreterException
	 */
	public InterpretedNormalObject(final IRSimpleClassUse type) throws InterpreterException {
		this.type = type;
		assert type.getBase() instanceof IRBrokkrClassDefinition;
		for (final IRMemberRedefinition m : type.getBase().members()) {
			if (m instanceof IRAttributeRedefinition && ((IRAttributeRedefinition) m).isVariable()) {
				final IRAttributeRedefinition attr = (IRAttributeRedefinition) m;
				final IRResultRedefinition singleResult = attr.getSingleResult();
				if (singleResult != null) {
					final IRExpression defaultValue = singleResult.defaultValue();
					if (defaultValue != null)
						attributeValues.put(attr.definition(), defaultValue.interpret(new InterpreterContext(type.getIRContext(), this)));
				}
				// TODO what about variable methods?
			}
		}
	}
	
	@Override
	public IRSimpleClassUse nativeClass() {
		return type;
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
