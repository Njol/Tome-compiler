package ch.njol.tome.compiler;

import ch.njol.tome.ast.ASTLink;

public final class LinkerError {
	
	public final ASTLink<?> link;
	
	public LinkerError(final ASTLink<?> link) {
		this.link = link;
	}
	
}
