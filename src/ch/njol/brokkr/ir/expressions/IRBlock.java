package ch.njol.brokkr.ir.expressions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpretedTuple;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClosure;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.brokkr.ir.statements.IRStatement;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

public class IRBlock extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final List<IRStatement> statements;
	
	public IRBlock(final IRContext irContext, final List<IRStatement> statements) {
		this.irContext = irContext;
		this.statements = registerDependencies(statements);
		IRElement.assertSameIRContext(Arrays.asList(this), statements);
	}
	
	public IRBlock(final IRContext irContext, final Stream<IRStatement> statements) {
		this(irContext, statements.collect(Collectors.toList()));
	}
	
	private IRTypeUse resultType() {
		// FIXME calculate result type from all possible return paths (TODO is that a tuple or a single type?)
		return new IRUnknownTypeUse(irContext);
	}
	
	@Override
	public IRTypeUse type() {
		// a block is a lambda without parameters
		// FIXME add generic parameters (empty arguments, but how many results?)
		// TODO find out if this block is modifying or not
		return irContext.getTypeUse("lang", "Procedure");
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		return new InterpretedNativeClosure(IRTypeTuple.emptyTuple(irContext), new IRTypeTupleBuilder(irContext).addEntry("result", resultType()).build(), true) {
			@Override
			protected InterpretedTuple interpret(final InterpretedTuple arguments) throws InterpreterException {
				for (final IRStatement statement : statements) {
					statement.interpret(context);
					if (context.isReturning)
						break;
					// TODO different 'return's for returning from the outermost function, and only returning from a single block?
				}
				return new InterpretedTuple(IRTypeTuple.emptyTuple(getIRContext()), Collections.EMPTY_LIST); // a block doesn't return anything - the result is stored in whatever result variable(s) is/are used
			}
		};
	}
	
	@Override
	public @NonNull IRContext getIRContext() {
		return irContext;
	}
	
}
