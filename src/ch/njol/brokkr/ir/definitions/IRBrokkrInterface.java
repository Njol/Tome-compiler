package ch.njol.brokkr.ir.definitions;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTInterfaceDeclaration;

/**
 * The native description of a Brokkr interface.
 */
public class IRBrokkrInterface extends AbstractIRBrokkrTypeDefinition {
	
	public IRBrokkrInterface(final ASTInterfaceDeclaration declaration) {
		super(declaration);
	}
	
}
