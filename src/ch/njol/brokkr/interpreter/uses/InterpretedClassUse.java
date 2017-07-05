package ch.njol.brokkr.interpreter.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

/**
 * The use of a class, with all generic parameters set (if any).
 */
public interface InterpretedClassUse extends InterpretedTypeUse {
	
	// TODO tuples are also classes (and interfaces), so this doesn't make much sense...
//	public InterpretedNativeClassDefinition getBase();
	
	public default @Nullable InterpretedAttributeImplementation getAttributeImplementation(final InterpretedAttributeDefinition definition) {
		final InterpretedMemberUse member = getMember(definition);
		if (member == null)
			return null;
		if (member instanceof InterpretedAttributeImplementation)
			return (InterpretedAttributeImplementation) member;
		throw new InterpreterException("Method " + definition + " not implemented in class " + this);
	}
	
}
