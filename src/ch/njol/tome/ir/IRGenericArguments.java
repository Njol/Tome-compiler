package ch.njol.tome.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRGenericTypeParameter;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRGenericArguments extends AbstractIRElement implements Comparable<IRGenericArguments> {
	
	private final IRContext irContext;
	
	private final Map<IRGenericTypeDefinition, IRTypeBoundGenericArgument> genericTypeBounds = new HashMap<>();
	
	private final Map<IRGenericTypeDefinition, IRExpression> genericTypeValues = new HashMap<>();
	
	private final Map<IRAttributeDefinition, IRExpression> attributeValues = new HashMap<>();
	
	private final List<IRExpression> predicates = new ArrayList<>();
	
	public IRGenericArguments(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	// TODO all add methods should check for duplicates
	
	public void addPredicateArgument(final IRPredicateGenericArgument predicate) {
		assert predicate.getIRContext() == irContext;
		predicates.add(predicate.predicate);
	}
	
	public void addTypeBoundArgument(final IRGenericTypeParameter parameter, final IRTypeBoundGenericArgument value) {
		assert parameter.getIRContext() == irContext;
		assert value.getIRContext() == irContext;
		genericTypeBounds.put(parameter.definition(), value);
	}
	
	public void addValueArgument(final IRMemberRedefinition member, final IRValueGenericArgument value) {
		assert member.getIRContext() == irContext;
		assert value.getIRContext() == irContext;
		if (member instanceof IRGenericTypeDefinition) {
			genericTypeValues.put((IRGenericTypeDefinition) member, value.value);
		} else {
			assert member instanceof IRAttributeRedefinition;
			attributeValues.put(((IRAttributeRedefinition) member).definition(), value.value);
		}
	}
	
	public @Nullable IRExpression getValueArgument(IRMemberRedefinition member) {
		if (member instanceof IRGenericTypeDefinition) {
			return genericTypeValues.get(member);
		} else {
			assert member instanceof IRAttributeRedefinition;
			return attributeValues.get(((IRAttributeRedefinition) member).definition());
		}
	}
	
	public IRGenericArguments combine(final IRGenericArguments other) {
		assert other.irContext == irContext;
		final IRGenericArguments combined = new IRGenericArguments(irContext);
		// FIXME do not just override generic attributes - combine them! (logical AND operation)
		return combined;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	/**
	 * @return true if this generic arguments has not a single argument defined
	 */
	public boolean isEmpty() {
		return genericTypeBounds.isEmpty() && genericTypeValues.isEmpty() && attributeValues.isEmpty() && predicates.isEmpty();
	}
	
	@Override
	public int compareTo(IRGenericArguments other) {
		// FIXME implement
//		final Iterator<Entry<IRGenericParameter, IRGenericArgument>> iter1 = genericArguments.entrySet().iterator(), iter2 = o.genericArguments.entrySet().iterator();
//		while (iter1.hasNext() && iter2.hasNext()) {
//			final Entry<IRGenericParameter, IRGenericArgument> e1 = iter1.next(), e2 = iter2.next();
//			final int c3 = e1.getKey().compareTo(e2.getKey());
//			if (c3 != 0)
//				return c3;
//			// FIXME
//			final int c4 = System.identityHashCode(e1.getValue()) - System.identityHashCode(e2.getValue());// e1.getValue().compareTo(e2.getValue());
//			if (c4 != 0)
//				return c4;
//		}
//		return iter1.hasNext() ? 1 : iter2.hasNext() ? -1 : 0;
		return 0;
	}
	
}
