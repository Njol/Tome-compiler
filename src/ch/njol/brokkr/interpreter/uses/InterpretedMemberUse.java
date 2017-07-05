package ch.njol.brokkr.interpreter.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;

public interface InterpretedMemberUse extends SourceCodeLinkable {
	
	InterpretedMemberRedefinition redefinition();
	
	InterpretedMemberDefinition definition();
	
	@Override
	default @Nullable ElementPart getLinked() {
		return redefinition().getLinked();
	}
	
}
