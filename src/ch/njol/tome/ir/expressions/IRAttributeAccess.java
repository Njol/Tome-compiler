package ch.njol.tome.ir.expressions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.InterpretedNullConstant;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpretedTuple;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeClosure;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.ir.definitions.IRAttributeImplementation;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.tome.ir.uses.IRTypeUse;

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
		this.target = registerDependency(target);
		this.attribute = registerDependency(attribute);
		this.arguments = arguments;
		registerDependencies(arguments.keySet());
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
					.getGenericUse("Arguments", IRValueGenericArgument.fromExpression(attribute.allParameterTypes(), null),
							"Results", IRValueGenericArgument.fromExpression(allResults ? attribute.allResultTypes()
									: new IRTypeTupleBuilder(getIRContext()).addEntry("result", attribute.mainResultType()).build(), null),
							null);
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
				final InterpretedObject result = ((IRAttributeImplementation) attribute).interpretImplementation(attribute.declaringType().getUse().interpret(context), args, allResults);
				if (result == null)
					throw new InterpreterException("Method has no results");
				return result;
			}
			return attribute.interpretDispatched(target, args, allResults);
		}
	}
	
}
