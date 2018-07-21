package ch.njol.tome.ir.expressions;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;

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
