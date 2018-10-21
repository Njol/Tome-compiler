package ch.njol.tome.ir.uses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;

public class IRGenericTypeUse extends AbstractIRTypeUse {
	
	private final IRTypeUse baseType;
	private final Map<IRAttributeDefinition, IRGenericArgument> genericArguments;
	
	public IRGenericTypeUse(final IRTypeUse use, final Map<IRAttributeDefinition, IRGenericArgument> genericArguments) {
		assert !(use instanceof IRGenericTypeUse);
		assert genericArguments.size() > 0;
		IRElement.assertSameIRContext(genericArguments.keySet(), genericArguments.values(), Arrays.asList(use));
		baseType = use;
		this.genericArguments = genericArguments;
	}
	
	public IRTypeUse getBaseType() {
		return baseType;
	}
	
	public Map<IRAttributeDefinition, IRGenericArgument> getGenericArguments() {
		return genericArguments;
	}
	
	@Override
	public IRGenericTypeUse getGenericUse(final Map<IRAttributeDefinition, IRGenericArgument> moreGenericArguments) {
		if (moreGenericArguments.isEmpty())
			return this;
		final HashMap<IRAttributeDefinition, IRGenericArgument> combinedGenerics = new HashMap<>();
		combinedGenerics.putAll(genericArguments);
		combinedGenerics.putAll(moreGenericArguments);
		// FIXME do not just override generic attributes - combine them! (logical AND operation)
		return new IRGenericTypeUse(baseType, combinedGenerics);
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRSimpleTypeUse && baseType.equalsType(((IRGenericTypeUse) other).baseType) && genericArguments.equals(((IRGenericTypeUse) other).genericArguments);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRGenericTypeUse) {
			final IRGenericTypeUse o = (IRGenericTypeUse) other;
			final int c = baseType.compareTo(o.baseType);
			if (c != 0)
				return c;
			final int c2 = genericArguments.size() - o.genericArguments.size();
			if (c2 != 0)
				return c2;
			final Iterator<Entry<IRAttributeDefinition, IRGenericArgument>> iter1 = genericArguments.entrySet().iterator(), iter2 = o.genericArguments.entrySet().iterator();
			while (iter1.hasNext() && iter2.hasNext()) {
				final Entry<IRAttributeDefinition, IRGenericArgument> e1 = iter1.next(), e2 = iter2.next();
				final int c3 = e1.getKey().compareTo(e2.getKey());
				if (c3 != 0)
					return c3;
				// FIXME
				final int c4 = System.identityHashCode(e1.getValue()) - System.identityHashCode(e2.getValue());// e1.getValue().compareTo(e2.getValue());
				if (c4 != 0)
					return c4;
			}
			return iter1.hasNext() ? 1 : iter2.hasNext() ? -1 : 0;
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return baseType.typeHashCode() + 31 * genericArguments.hashCode();
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return baseType.allInterfaces().stream().map(i -> {
			return i.getGenericUse(genericArguments);
		}).collect(Collectors.toSet());
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		// TODO check generic (also define how this method works for the different type uses)
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return baseType.isSupertypeOfOrEqual(other);
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return baseType.members().stream().map(mu -> mu.getGenericUse(genericArguments)).collect(Collectors.toList());
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		// TODO do the generics give any info at runtime?
		return baseType.interpret(context);
	}
	
	@Override
	public IRContext getIRContext() {
		return baseType.getIRContext();
	}
	
	@Override
	public String toString() {
		return baseType + "<" + genericArguments.entrySet().stream().map(e -> e.getKey().name() + ": " + e.getValue()).collect(Collectors.joining(", ", "", "")) + ">";
	}
	
}
