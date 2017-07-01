package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;

public class InterpretedBrokkrGenericTypeRedefinition extends AbstractInterpretedBrokkrGenericType {

	private final InterpretedGenericTypeRedefinition parent;

	public InterpretedBrokkrGenericTypeRedefinition(GenericTypeDeclaration declaration, InterpretedGenericTypeRedefinition parent) {
		super(declaration);
		this.parent = parent;
	}

	@Override
	public InterpretedGenericTypeDefinition definition() {
		return parent.definition();
	}
	
}
