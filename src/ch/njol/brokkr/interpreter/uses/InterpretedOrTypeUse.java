package ch.njol.brokkr.interpreter.uses;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;

public class InterpretedOrTypeUse implements InterpretedTypeUse {
	
	// TODO use an arbitrary amount of types?
	private final InterpretedTypeUse t1, t2;
	
	public InterpretedOrTypeUse(final InterpretedTypeUse t1, final InterpretedTypeUse t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public @Nullable InterpretedMemberRedefinition getMemberByName(String name) {
		InterpretedMemberRedefinition m1 = t1.getMemberByName(name), m2 = t2.getMemberByName(name);
		return m1 != null && m1.equalsMember(m2) ? m1 : null; // TODO what if one member is a "super-member" of the other?
	}

	@Override
	public boolean equalsType(@NonNull InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubtypeOfOrEqual(@NonNull InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupertypeOfOrEqual(@NonNull InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<? extends InterpretedMemberRedefinition> members() {
		// TODO find common members
		return Collections.EMPTY_LIST;
	}

}
