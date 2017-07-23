package ch.njol.brokkr.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;

public interface IRMemberUse extends SourceCodeLinkable {
	
	IRMemberRedefinition redefinition();
	
	IRMemberDefinition definition();
	
	@Override
	default @Nullable ASTElementPart getLinked() {
		return redefinition().getLinked();
	}
	
}
