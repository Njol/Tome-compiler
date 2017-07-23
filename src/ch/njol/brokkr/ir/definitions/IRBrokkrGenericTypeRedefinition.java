package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;

public class IRBrokkrGenericTypeRedefinition extends AbstractIRBrokkrGenericType {
	
	private final IRGenericTypeRedefinition parent;
	
	public IRBrokkrGenericTypeRedefinition(final ASTGenericTypeDeclaration declaration, final IRGenericTypeRedefinition parent) {
		super(declaration);
		this.parent = parent;
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		return parent.definition();
	}
	
}
