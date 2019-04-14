package ch.njol.tome.ir.uses;

import java.util.List;
import java.util.Set;

import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRGenericArguments;
import ch.njol.tome.ir.definitions.IRGenericTypeDefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRGenericTypeUse extends AbstractIRTypeUse {
	
	private final IRTypeUse target;
	private final IRGenericTypeDefinition definition;
	
	public IRGenericTypeUse(final IRTypeUse target, final IRGenericTypeDefinition definition) {
		IRElement.assertSameIRContext(target, definition);
		this.target = target;
		this.definition = definition;
	}
	
	private IRTypeUse getUse() {
		if (target instanceof IRTypeUseWithGenerics) {
			IRGenericArguments genericArguments = ((IRTypeUseWithGenerics) target).getGenericArguments();
			IRExpression value = genericArguments.getValueArgument(definition);
			if (value instanceof IRTypeUse)
				return (IRTypeUse) value;
		}
		// TODO others? also better put this into the interface IRTypeUse...
		IRTypeUse extended = definition.extends_();
		return extended != null ? extended : getIRContext().getTypeUse("lang", "Any");
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return getUse().allInterfaces();
	}
	
	@Override
	public int typeHashCode() {
		return target.typeHashCode() * 31 + definition.memberHashCode();
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRGenericTypeUse
				&& target.equalsType(((IRGenericTypeUse) other).target)
				&& definition.equalsMember(((IRGenericTypeUse) other).definition);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRGenericTypeUse) {
			int c = target.compareTo(((IRGenericTypeUse) other).target);
			if (c != 0)
				return c;
			return definition.compareTo(((IRGenericTypeUse) other).definition);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return getUse().members(); // TODO should probably add this generic type somewhere?
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented and may not even make sense");
	}
	
	@Override
	public IRContext getIRContext() {
		return target.getIRContext();
	}
	
}
