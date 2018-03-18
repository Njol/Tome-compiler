package ch.njol.brokkr.ir.expressions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedNullConstant;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpretedTuple;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClosure;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRAttributeAccess extends AbstractIRExpression {
	
	private final @Nullable IRExpression target;
	private final IRAttributeRedefinition attribute;
	private final Map<IRParameterDefinition, IRExpression> arguments;
	private final boolean allResults;
	private final boolean nullSafe;
	private final boolean meta;
	
	public IRAttributeAccess(@Nullable final IRExpression target, final IRAttributeRedefinition attribute, final Map<IRParameterDefinition, IRExpression> arguments,
			final boolean allResults, final boolean nullSafe, final boolean meta) {
		IRElement.assertSameIRContext(target == null ? Arrays.asList(attribute) : Arrays.asList(target, attribute), arguments.keySet(), arguments.values());
		this.target = target;
		this.attribute = attribute;
		this.arguments = arguments;
		this.allResults = allResults;
		this.nullSafe = nullSafe;
		this.meta = meta;
	}
	
	@Override
	public IRTypeUse type() {
		if (meta) {
			// TODO check whether params are modifiable too?
			// TODO return a tuple of one element when not using all results, or just the result itself?
			// TODO remove passed arguments from resulting arguments tuple
			return getIRContext().getTypeDefinition("lang", attribute.isModifying() ? "Procedure" : "Function")
					.getUse("Arguments", attribute.allParameterTypes(),
							"Results", allResults ? attribute.allResultTypes() : new IRTypeTupleBuilder(getIRContext()).addEntry("result", attribute.mainResultType()).build(), null);
		} else {
			return allResults ? attribute.allResultTypes() : attribute.mainResultType();
		}
	}
	
	@Override
	public IRContext getIRContext() {
		return attribute.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		final InterpretedObject target = this.target != null ? this.target.interpret(context) : null;
		if (nullSafe && target instanceof InterpretedNullConstant)
			return target;
		final Map<IRParameterDefinition, InterpretedObject> args = new HashMap<>();
		if (meta) {
			final IRTypeTuple remainingParams = attribute.allParameterTypes();
			final IRTypeTuple results = allResults ? attribute.allResultTypes() : new IRTypeTupleBuilder(getIRContext()).addEntry("result", attribute.mainResultType()).build();
			return new InterpretedNativeClosure(remainingParams, results, attribute.isModifying()) {
				@Override
				protected InterpretedTuple interpret(final InterpretedTuple remainingArguments) throws InterpreterException {
					throw new InterpreterException("not implemented");//TODO
				}
			};
		} else {
			if (target == null) {
				if (!(attribute instanceof IRAttributeImplementation))
					throw new InterpreterException("Trying to invoke a static method that has no body");
				final InterpretedObject result = ((IRAttributeImplementation) attribute).interpretImplementation(attribute.declaringType().getRawUse().interpret(context), args, allResults);
				if (result == null)
					throw new InterpreterException("Method has no results");
				return result;
			}
			return attribute.interpretDispatched(target, args, allResults);
		}
	}
	
}
