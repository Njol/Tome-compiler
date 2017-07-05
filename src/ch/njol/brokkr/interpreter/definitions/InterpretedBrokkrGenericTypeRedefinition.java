package ch.njol.brokkr.interpreter.definitions;

import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;

public class InterpretedBrokkrGenericTypeRedefinition extends AbstractInterpretedBrokkrGenericType {
	
	private final InterpretedGenericTypeRedefinition parent;
	
	public InterpretedBrokkrGenericTypeRedefinition(final GenericTypeDeclaration declaration, final InterpretedGenericTypeRedefinition parent) {
		super(declaration);
		this.parent = parent;
	}
	
	@Override
	public InterpretedGenericTypeDefinition definition() {
		return parent.definition();
	}
	
}
