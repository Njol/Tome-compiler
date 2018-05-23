package ch.njol.brokkr.ir.expressions;

import java.util.ArrayList;
import java.util.List;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpretedTuple;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRArgumentsKeyword extends AbstractIRExpression {
	
	private final IRAttributeRedefinition attribute;
	
	public IRArgumentsKeyword(final IRAttributeRedefinition attribute) {
		this.attribute = registerDependency(attribute);
	}
	
	@Override
	public IRTypeUse type() {
		return attribute.allParameterTypes();
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		final List<InterpretedObject> values = new ArrayList<>();
		for (final IRParameterRedefinition parameter : attribute.parameters()) {
			values.add(context.getLocalVariableValue(parameter.definition()));
		}
		return new InterpretedTuple(attribute.allParameterTypes(), values);
	}
	
}
