package ch.njol.brokkr.ir.statements;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;

public abstract class AbstractIRPreOrPostCondition extends AbstractIRStatement {
	
	private final IRAttributeRedefinition attribute;
	protected final @Nullable String name;
	private final IRExpression value;
	
	public AbstractIRPreOrPostCondition(final IRAttributeRedefinition attribute, final @Nullable String name, final IRExpression value) {
		IRElement.assertSameIRContext(attribute, value);
		this.attribute = attribute;
		this.name = name;
		this.value = value;
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		if (!InterpretedNativeBoolean.getBoolean(context, value.interpret(context)))
			throw new InterpreterException((this instanceof IRPrecondition ? "Pre" : "Post") + "condition " + (name != null ? name : "<unnamed>") + " of attribute " + attribute + " not satisfied");
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
}
