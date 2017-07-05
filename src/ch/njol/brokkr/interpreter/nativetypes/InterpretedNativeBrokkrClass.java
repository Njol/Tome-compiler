package ch.njol.brokkr.interpreter.nativetypes;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.AbstractInterpretedNativeBrokkrTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;

public class InterpretedNativeBrokkrClass extends AbstractInterpretedNativeBrokkrTypeDefinition implements InterpretedNativeClassDefinition {
	
	public InterpretedNativeBrokkrClass(final TypeDeclaration declaration) {
		super(declaration);
	}
	
	@Override
	public @Nullable InterpretedAttributeImplementation getAttributeImplementation(@NonNull final InterpretedAttributeDefinition definition) {
		for (final InterpretedMemberRedefinition m : members) {
			if (m instanceof InterpretedAttributeImplementation && ((InterpretedAttributeImplementation) m).definition().equalsMember(definition))
				return (InterpretedAttributeImplementation) m;
		}
		return null;
	}
	
}
