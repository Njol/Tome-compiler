package ch.njol.brokkr.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;

public abstract class AbstractIRUnknown extends AbstractIRElement {
	
	protected final String errorMessage;
	protected final @Nullable ASTElementPart location;
	private final IRContext irContext;
	
	public AbstractIRUnknown(final String errorMessage, final ASTElementPart location) { // TODO allow arbitrary error index + length (need to find a better way than to use ints first though)
		this.errorMessage = errorMessage;
		this.location = location;
		irContext = location.getIRContext();
	}
	
	public AbstractIRUnknown(final String errorMessage, final @Nullable ASTElementPart location, final IRContext irContext) {
		this.errorMessage = errorMessage;
		this.location = location;
		this.irContext = irContext;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
}
