package ch.njol.tome.ir.expressions;

import java.util.Arrays;
import java.util.List;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpretedTuple;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeClosure;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTupleBuilderEntry;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRClosure extends AbstractIRExpression {
	
	private final List<? extends IRVariableRedefinition> parameters;
	private final IRTypeTuple parameterTypes;
	private final IRExpression value;
	
	public IRClosure(final List<? extends IRVariableRedefinition> parameters, final IRExpression value) {
		IRElement.assertSameIRContext(parameters, value);
		this.parameters = registerDependencies(parameters);
		registerDependencies(parameters);
		parameterTypes = new IRTypeTuple(value.getIRContext(), parameters.stream().map(p -> new IRTupleBuilderEntry(p.name(), p.type())));
		this.value = registerDependency(value);
	}
	
	@Override
	public IRTypeUse type() {
		// TODO modifying/nonmodifying
		return getIRContext().getTypeDefinition("lang", "Function")
				.getGenericUse(
						"Arguments", IRValueGenericArgument.fromExpression(parameterTypes, null),
						"Results", IRValueGenericArgument.fromExpression(value.type(), null),
						null);
	}
	
	@Override
	public IRContext getIRContext() {
		return value.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		return new InterpretedNativeClosure(parameterTypes, new IRTypeTupleBuilder(getIRContext()).addEntry("result", value.type()).build(), false) {
			@Override
			protected InterpretedTuple interpret(final InterpretedTuple arguments) throws InterpreterException {
				final InterpreterContext localContext = new InterpreterContext(context.irContext, null);
				for (int i = 0; i < parameters.size(); i++) // FIXME don't use order, use names/definitions
					localContext.defineLocalVariable(parameters.get(i).definition(), arguments.values.get(i));
				return new InterpretedTuple(new IRTypeTupleBuilder(getIRContext()).addEntry("result", value.type()).build(), Arrays.asList(value.interpret(localContext)));
			}
		};
	}
	
}
