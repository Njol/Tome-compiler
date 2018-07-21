package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;

public class IRUnknownGenericArgument extends AbstractIRUnknown implements IRGenericArgument {

	public IRUnknownGenericArgument(String errorMessage, ASTElementPart location) {
		super(errorMessage, location);
	}

	public IRUnknownGenericArgument(String errorMessage, @Nullable ASTElementPart location, IRContext irContext) {
		super(errorMessage, location, irContext);
	}
	
}
