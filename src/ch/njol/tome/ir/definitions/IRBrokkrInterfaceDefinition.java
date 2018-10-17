package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.toplevel.ASTInterfaceDeclaration;

/**
 * The native description of a Brokkr interface.
 */
public class IRBrokkrInterfaceDefinition extends AbstractIRBrokkrTypeDefinition {
	
	public IRBrokkrInterfaceDefinition(final ASTInterfaceDeclaration declaration) {
		super(declaration);
	}
	
}
