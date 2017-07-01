package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;

public class InterpretedBrokkrGenericTypeDefinition extends AbstractInterpretedBrokkrGenericType implements InterpretedGenericTypeDefinition {

	public InterpretedBrokkrGenericTypeDefinition(GenericTypeDeclaration declaration) {
		super(declaration);
	}
	
}
