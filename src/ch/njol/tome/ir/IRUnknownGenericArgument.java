package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;

public class IRUnknownGenericArgument extends AbstractIRUnknown implements IRGenericArgument {
	
	public IRUnknownGenericArgument(final String errorMessage, final ASTElementPart location) {
		super(errorMessage, location);
	}
	
	public IRUnknownGenericArgument(final String errorMessage, @Nullable final ASTElementPart location, final IRContext irContext) {
		super(errorMessage, location, irContext);
	}
	
}
