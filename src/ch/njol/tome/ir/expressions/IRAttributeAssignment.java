package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRAttributeAssignment extends AbstractIRExpression {
	
	private final IRExpression target;
	private final IRAttributeDefinition attribute;
	private final IRExpression value;
	
	public IRAttributeAssignment(final IRExpression target, final IRAttributeDefinition attribute, final IRExpression value) {
		IRElement.assertSameIRContext(target, attribute, value);
		this.target = registerDependency(target);
		this.attribute = registerDependency(attribute);
		this.value = registerDependency(value);
	}
	
	@Override
	public IRTypeUse type() {
		return value.type();
	}
	
	@Override
	public IRContext getIRContext() {
		return target.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		// semantics: first the target is evaluated, then the value, then the assignment happens. TODO enforce target to not have side-effects to prevent this from mattering?
		// The value of the assignment expression is the assigned value (some languages instead use the old value, but that is quite counter-intuitive)
		final InterpretedObject target = this.target.interpret(context);
		final InterpretedObject val = value.interpret(context);
		if (!(target instanceof InterpretedNormalObject))
			throw new InterpreterException("Trying to assign an attribute on a non-Brokkr object");
		if (!attribute.isVariable())
			throw new InterpreterException("Trying to assign to a non-variable attribute");
		((InterpretedNormalObject) target).setAttributeValue(attribute, val);
		return val;
	}
	
}
