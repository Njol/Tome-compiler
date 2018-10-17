package ch.njol.tome.ir.expressions;

import ch.njol.tome.ast.expressions.ASTKleeneanConstant;
import ch.njol.tome.common.Kleenean;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeKleenean;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRKleeneanConstant extends AbstractIRExpression {
	
	private final IRContext irContext;
	private final Kleenean value;
	
	public IRKleeneanConstant(final ASTKleeneanConstant ast) {
		irContext = ast.getIRContext();
		value = registerDependency(ast).value;
	}
	
	@Override
	public IRTypeUse type() {
		return irContext.getTypeUse("lang", value == Kleenean.UNKNOWN ? "Kleenean" : "Boolean");
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) {
		if (value == Kleenean.UNKNOWN)
			return new InterpretedNativeKleenean(irContext, Kleenean.UNKNOWN);
		else
			return new InterpretedNativeBoolean(irContext, value == Kleenean.TRUE);
	}
	
}
