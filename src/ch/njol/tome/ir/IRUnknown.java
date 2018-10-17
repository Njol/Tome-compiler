package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;

public interface IRUnknown extends IRElement {
	
	public String getErrorMessage();
	
	public @Nullable ASTElementPart getLocation();
	
}
