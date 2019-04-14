package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRGenericTypeDefinition extends IRMemberDefinition {
	
	public @Nullable IRTypeUse extends_();
	
	public @Nullable IRTypeUse super_();
	
}
