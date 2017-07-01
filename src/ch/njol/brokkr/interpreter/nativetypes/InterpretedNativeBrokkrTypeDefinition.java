package ch.njol.brokkr.interpreter.nativetypes;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

/**
 * The native description of a type that has a declaration (i.e. is neither a composite type nor a tuple type)
 */
public interface InterpretedNativeBrokkrTypeDefinition extends InterpretedNativeTypeDefinition {

}
