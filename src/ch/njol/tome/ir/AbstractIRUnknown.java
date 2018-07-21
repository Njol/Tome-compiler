package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;

public abstract class AbstractIRUnknown extends AbstractIRElement {
	
	protected final String errorMessage;
	protected final @Nullable ASTElementPart location;
	private final IRContext irContext;
	
	public AbstractIRUnknown(final String errorMessage, final ASTElementPart location) { // TODO allow arbitrary error index + length (need to find a better way than to use ints first though)
		this.errorMessage = errorMessage;
		this.location = location;
		if (location instanceof ASTElement)
			registerDependency((ASTElement) location);
		irContext = location.getIRContext();
	}
	
	public AbstractIRUnknown(final String errorMessage, final @Nullable ASTElementPart location, final IRContext irContext) {
		this.errorMessage = errorMessage;
		this.location = location;
		if (location instanceof ASTElement)
			registerDependency((ASTElement) location);
		this.irContext = irContext;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
}
