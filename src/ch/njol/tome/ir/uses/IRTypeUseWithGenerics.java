package ch.njol.tome.ir.uses;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRGenericArguments;

public class IRTypeUseWithGenerics extends AbstractIRTypeUse {
	
	private final IRTypeUse baseType;
	private final IRGenericArguments genericArguments;
	
	public IRTypeUseWithGenerics(final IRTypeUse use, final IRGenericArguments genericArguments) {
		assert !(use instanceof IRTypeUseWithGenerics);
		assert !genericArguments.isEmpty();
		IRElement.assertSameIRContext(genericArguments, use);
		baseType = use;
		this.genericArguments = genericArguments;
	}
	
	public IRTypeUse getBaseType() {
		return baseType;
	}
	
	public IRGenericArguments getGenericArguments() {
		return genericArguments;
	}
	
	@Override
	public IRTypeUseWithGenerics getGenericUse(final IRGenericArguments moreGenericArguments) {
		if (moreGenericArguments.isEmpty())
			return this;
		return new IRTypeUseWithGenerics(baseType, genericArguments.combine(moreGenericArguments));
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRSimpleTypeUse && baseType.equalsType(((IRTypeUseWithGenerics) other).baseType) && genericArguments.equals(((IRTypeUseWithGenerics) other).genericArguments);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRTypeUseWithGenerics) {
			final IRTypeUseWithGenerics o = (IRTypeUseWithGenerics) other;
			final int c = baseType.compareTo(o.baseType);
			if (c != 0)
				return c;
			return genericArguments.compareTo(o.genericArguments);
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
		return baseType + "<" + genericArguments + ">";
	}
	
}
