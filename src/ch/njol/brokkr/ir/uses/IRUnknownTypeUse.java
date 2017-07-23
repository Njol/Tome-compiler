package ch.njol.brokkr.ir.uses;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 *¨An unresolvable type (usually due to syntax or semantic errors in the code)
 */
public class IRUnknownTypeUse implements IRTypeUse {
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public @NonNull String toString() {
		return "<unresolvable type>";
	}
	
}
