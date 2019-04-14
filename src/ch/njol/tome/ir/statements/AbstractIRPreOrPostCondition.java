package ch.njol.tome.ir.statements;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.expressions.IRExpression;

public abstract class AbstractIRPreOrPostCondition extends AbstractIRStatement {
	
	private final IRAttributeRedefinition attribute;
	protected final @Nullable String name;
	private final IRExpression value;
	
	public AbstractIRPreOrPostCondition(final IRAttributeRedefinition attribute, final @Nullable String name, final IRExpression value) {
		IRElement.assertSameIRContext(attribute, value);
		this.attribute = registerDependency(attribute);
		this.name = name;
		this.value = registerDependency(value);
	}
	
	@Override
	public void interpret(final InterpreterContext context) throws InterpreterException {
		if (!InterpretedNativeBoolean.getBoolean(context, value.interpret(context)))
			throw new InterpreterException((this instanceof IRPreconditionDeclaration ? "Pre" : "Post") + "condition " + (name != null ? name : "<unnamed>") + " of attribute " + attribute + " not satisfied");
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
}
