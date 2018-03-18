package ch.njol.brokkr.ir.uses;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.AbstractIRElement;

public abstract class AbstractIRTypeUse extends AbstractIRElement implements IRTypeUse {
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (!(obj instanceof IRTypeUse))
			return false;
		return equalsType((IRTypeUse) obj);
	}
	
	@Override
	public int hashCode() {
		return typeHashCode();
	}
	
	@Override
	public IRTypeUse type() {
		return new IRTypeUseClassUse(this);
	}
	
}
