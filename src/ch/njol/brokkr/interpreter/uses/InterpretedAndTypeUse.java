package ch.njol.brokkr.interpreter.uses;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;

public class InterpretedAndTypeUse implements InterpretedTypeUse {
	
	// TODO use an arbitrary amount of types?
	private final InterpretedTypeUse t1, t2;
	
	public InterpretedAndTypeUse(final InterpretedTypeUse t1, final InterpretedTypeUse t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public boolean equalsType(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<? extends InterpretedMemberRedefinition> members() {
		final List<? extends InterpretedMemberRedefinition> ms1 = t1.members(), ms2 = t2.members();
		final @NonNull List<InterpretedMemberRedefinition> result = new ArrayList<>(ms1);
		outer: for (final InterpretedMemberRedefinition m2 : ms2) {
			for (final InterpretedMemberRedefinition m1 : ms1) {
				if (m1.definition().equals(m2.definition())) {
					// TODO make sure there's no conflict, and if there's none, find which attribute to use
					break outer;
				}
			}
			result.add(m2);
		}
		return result;
	}
	
}
