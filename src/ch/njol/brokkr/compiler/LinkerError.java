package ch.njol.brokkr.compiler;

import ch.njol.brokkr.compiler.ast.Link;

public final class LinkerError {
	
	public final Link<?> link;
	
	public LinkerError(final Link<?> link) {
		this.link = link;
	}
	
}
