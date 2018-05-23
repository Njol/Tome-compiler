package ch.njol.brokkr.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

public interface IRMemberUse extends SourceCodeLinkable, IRElement {
	
	IRMemberRedefinition redefinition();
	
	IRMemberDefinition definition();
	
	default IRMemberRedefinition getRedefinitionFor(final IRTypeDefinition forType) {
		return redefinition();
	}
	
	@Override
	default @Nullable ASTElementPart getLinked() {
		return redefinition().getLinked();
	}
	
}
