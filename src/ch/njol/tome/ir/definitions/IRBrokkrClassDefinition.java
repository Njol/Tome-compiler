package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;

public class IRBrokkrClassDefinition extends AbstractIRBrokkrTypeDefinition implements IRClassDefinition {
	
	public IRBrokkrClassDefinition(final ASTTypeDeclaration<?> declaration) {
		super(declaration);
	}
	
	@Override
	public @Nullable IRAttributeImplementation getAttributeImplementation(@NonNull final IRAttributeDefinition definition) {
		for (final IRMemberRedefinition m : members()) {
			if (m instanceof IRAttributeImplementation && ((IRAttributeImplementation) m).definition().equalsMember(definition))
				return (IRAttributeImplementation) m;
		}
		return null;
	}
	
}
