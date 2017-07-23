package ch.njol.brokkr.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;

/**
 * The use of a class, with all generic parameters set (if any).
 */
public interface IRClassUse extends IRTypeUse {
	
	// TODO tuples are also classes (and interfaces), so this doesn't make much sense...
//	public IRNativeClassDefinition getBase();
	
	public default @Nullable IRAttributeImplementation getAttributeImplementation(final IRAttributeDefinition definition) {
		final IRMemberUse member = getMember(definition);
		if (member == null)
			return null;
		if (member instanceof IRAttributeImplementation)
			return (IRAttributeImplementation) member;
		throw new InterpreterException("Method " + definition + " not implemented in class " + this);
	}
	
}
