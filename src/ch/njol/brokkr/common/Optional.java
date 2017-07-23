package ch.njol.brokkr.common;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.AbstractASTElement;

public enum Optional {
	
	NULLABLE, NONNULL;
	
	public static @Nullable Optional parse(final AbstractASTElement<?> parent) {
		if (parent.try_("nullable"))
			return NULLABLE;
		if (parent.try_("nonnull"))
			return NONNULL;
		return null;
	}
	
}
