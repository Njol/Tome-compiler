package ch.njol.tome.ast;

import ch.njol.tome.ast.ASTInterfaces.ASTElementWithIR;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.util.Cache;

public abstract class AbstractASTElementWithIR<IR extends IRElement> extends AbstractASTElement implements ASTElementWithIR<IR> {
	
	protected final Cache<IR> irCache = new Cache<>(this, this::calculateIR);
	
	@Override
	public final Cache<IR> irChache() {
		return irCache;
	}
	
	protected abstract IR calculateIR();
	
	@Override
	public final IR getIR() {
		return irCache.get();
	}
	
}
