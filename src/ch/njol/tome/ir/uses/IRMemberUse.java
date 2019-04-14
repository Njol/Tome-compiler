package ch.njol.tome.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRGenericArguments;
import ch.njol.tome.ir.definitions.IRMemberDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;

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
	
	default IRMemberUse getGenericUse(final IRGenericArguments arguments) {
		return this; // FIXME implement
	}
	
}
