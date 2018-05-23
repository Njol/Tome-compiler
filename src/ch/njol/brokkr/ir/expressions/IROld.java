package ch.njol.brokkr.ir.expressions;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IROld extends AbstractIRExpression {
	
	private final IRAttributeRedefinition attribute;
	private final IRExpression expression;
	
	public IROld(final IRAttributeRedefinition attribute, final IRExpression expression) {
		IRElement.assertSameIRContext(attribute, expression);
		this.attribute = registerDependency(attribute);
		this.expression = registerDependency(expression);
	}
	
	@Override
	public IRTypeUse type() {
		return expression.type();
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented");
	}
	
}
