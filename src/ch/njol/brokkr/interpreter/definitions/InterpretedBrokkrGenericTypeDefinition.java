package ch.njol.brokkr.interpreter.definitions;

import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;

public class InterpretedBrokkrGenericTypeDefinition extends AbstractInterpretedBrokkrGenericType implements InterpretedGenericTypeDefinition {
	
	public InterpretedBrokkrGenericTypeDefinition(final GenericTypeDeclaration declaration) {
		super(declaration);
	}
	
}
