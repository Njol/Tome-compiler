package ch.njol.brokkr.compiler;

import ch.njol.brokkr.ast.ASTLink;

public final class LinkerError {
	
	public final ASTLink<?> link;
	
	public LinkerError(final ASTLink<?> link) {
		this.link = link;
	}
	
}
