package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTInterfaceDeclaration;

/**
 * The native description of a Brokkr interface.
 */
public class IRBrokkrInterfaceDefinition extends AbstractIRBrokkrTypeDefinition {
	
	public IRBrokkrInterfaceDefinition(final ASTInterfaceDeclaration declaration) {
		super(declaration);
	}
	
}
