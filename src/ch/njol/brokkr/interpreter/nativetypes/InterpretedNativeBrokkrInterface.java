package ch.njol.brokkr.interpreter.nativetypes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.TopLevelElements.InterfaceDeclaration;
import ch.njol.brokkr.interpreter.definitions.AbstractInterpretedNativeBrokkrTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;

/**
 * The native description of a Brokkr interface.
 */
public class InterpretedNativeBrokkrInterface extends AbstractInterpretedNativeBrokkrTypeDefinition {
	
	public InterpretedNativeBrokkrInterface(final InterfaceDeclaration declaration) {
		super(declaration);
	}

}
